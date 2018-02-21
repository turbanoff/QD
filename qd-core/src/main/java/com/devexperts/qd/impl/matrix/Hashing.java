/*
 * !++
 * QDS - Quick Data Signalling Library
 * !-
 * Copyright (C) 2002 - 2018 Devexperts LLC
 * !-
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * !__
 */
package com.devexperts.qd.impl.matrix;

/**
 * The <code>Hashing</code> contains constants and algorithms to support
 * various matrix-based data structures. These algorithms are also known
 * as "direct linear probe hashing".
 * <p>
 * The idea of the matrix is the same as in the common hash-based collections:
 * using key hashcode calculate initial entry index in the array and then
 * navigate corresponding list of entries until matching key is found.
 * The difference is that while common hash-based collections build explicit
 * linked lists of entries, the matrix builds such lists directly within
 * used array, usually by repeatedly stepping entry index.
 * <p>
 * This algorithm, however, has complications resulting from the fact that
 * entry lists for different initial indices mix with each other. As a result,
 * the lists became longer and remove operation became much more complex.
 * Depending on overall fill factor, the cost of search operation is:
 * <pre>
 * fill factor         = ff
 * average hit length  = (1 + 1 / (1 - ff)) / 2
 * average miss length = (1 + 1 / (1 - ff) ^ 2) / 2
 *
 * ff    hit    miss
 * 3/4   2.5    8.5
 * 2/3   2      5
 * 1/2   1.5    2.5
 * 1/3   1.25   1.63
 * 1/4   1.17   1.39</pre>
 * The mixing problem also requires that choosing of initial index must have
 * very high dispersion - i.e. it must hash the key equally well over all
 * possible ranges. To achieve this, the original key hashcode is multiplied
 * by large number (the MAGIC) with good bit density and dispersion
 * before it is scaled onto used array. The scaling must use high bits of the
 * product as they have better randomization. To perform good and fast scaling,
 * the right binary shift of product is used and the underlying array has length
 * which is a power of 2. The resulting formula for initial index looks like:
 * <pre>index = (hashcode * MAGIC) >>> shift;</pre>
 * where 'shift' is a specially calculated value based on used array length.
 * The common index stepping algorithm is to decrement index by 1 (switching
 * to the end of the array when 0 is reached) until matching key is found or
 * empty entry encountered.
 * <p>
 * The plain matrix idea may be improved in a number of ways:
 * <li>Explicitly placing into array multiple parts of a composite keys and values -
 * this increases entry size and index step, but saves individual objects for keys
 * and values. This explicit decomposition forces obligatory use of explicit step for
 * index and entry size (in the examples above the step was implicitly equal to 1).
 * For this purpose, the variable <code>step</code> is introduced throughout code.
 * <li>Deliberately delaying actual remove operation until matrix is rehashed -
 * this uses some memory for storing garbage, but simplifies used algorithms and
 * allows unsynchronized reads from the matrix. This approach replaces single size
 * of the matrix with two distinct sizes: <code>overall_size</code> (the number
 * of all entries, including removed) and <code>payload_size</code> (the number
 * of valuable entries, excluding removed), reflected in corresponding variables.
 * <p>
 * The <code>Hashing</code> defines algorithms and constraints that allow
 * matrix-based collections to have high performance, acceptable payload
 * fill factor and low amortized cost of operations:
 * <li>To achieve high performance, the upper limit for overall fill factor is 2/3,
 * thus in worst case average hit length is 2 and average miss length is 5.
 * <li>To achieve acceptable payload fill factor, the lower limit for
 * payload fill factor is 2/9, thus payload fill factor lies in [2/9, 2/3]
 * and on average is about 1/3 or higher.
 * <li>To achieve low amortized cost, the shift search algorithm produces
 * initial fill factors from 5/18 to 5/9, thus the need of successive rehash
 * may arise only after addition or removal of at least 20% of existing entries.
 * <p>
 * <b>NOTE:</b> there is a common implementation approach exist to use index 0
 * for 'void' entry. According to this approach, the entry with index 0 is never
 * used to hold actual data. All its elements always contain 0 or null as
 * appropriate. This approach simplifies implementation in a number of ways:
 * <li>It treats all uninitialized indices as 'void'.
 * <li>It allows fast presence check.
 * <li>It allows efficient remapping of misses using <i>miss_mask</i>.
 * <li>It allows safe reads by this index with guaranteed 'void' values.
 * <br>(The <i>miss_mask</i> is a binary mask applied to index if it was not found;
 * its usage allows the same method to be used both to find index and to add index.)
 */
final class Hashing {

	static final int MAGIC_RID = 0x5F3AC769; // Magic number used in hashing for rid.

	static final int THRESHOLD_UP = (int)((1L << 32) * 2 / 3);
	static final int THRESHOLD_DOWN = (int)((1L << 32) * 2 / 9);

	static final int THRESHOLD_ALLOC_UP = (int)((1L << 32) * 5 / 9);
	static final int MAX_SHIFT = 29;
	static final int MIN_LENGTH = 1 << (32 - MAX_SHIFT);

	/**
	 * Calculates appropriate 'shift' for specified capacity.
	 */
	static int getShift(int capacity) {
		int shift = MAX_SHIFT;
		while ((THRESHOLD_ALLOC_UP >>> shift) < capacity && shift > 1)
			shift--;
		if (shift <= 1)
			throw new IllegalArgumentException("Capacity is too large: " + capacity);
		return shift;
	}

	private static final int MAGIC = 0xB46394CD; // Magic number used to generate pseudo-random magic numbers.

	// Use "random" seed by default, but allow to specify a fixed value in system properties for testing purposes
	private static int seed = getSeed();

	private static int getSeed() {
		int def = (int)(System.currentTimeMillis() * Runtime.getRuntime().freeMemory());
		try {
			return Integer.getInteger("com.devexperts.qd.impl.matrix.Hashing.seed", def);
		} catch (SecurityException e) {
			return def; // ignore
		}
	}

	/**
	 * Generates next MAGIC number with proper distribution and difference of bits.
	 */
	static int nextMagic(int prev_magic) {
		// Generate next pseudo-random number with lowest bit set to '1'.
		int magic = (seed = seed * MAGIC + 1) | 1;
		// Enforce that any 4 bits are neither '0000' nor '1111'.
		// Start earlier to enforce that highest 2 bits are neither '00' nor '11'.
		for (int i = 31; --i >= 0;) {
			int bits = (magic >> i) & 0x0F;
			if (bits == 0 || bits == 0x0F) {
				magic ^= 1 << i;
				i -= 2;
			}
		}
		// Recover cleared lowest bit.
		if ((magic & 1) == 0)
			magic ^= 3; // Convert '10' (the only possible case) into '01'.
		// Enforce that any 8 bits have at least 1 difference from previous number.
		for (int i = 25; --i >= 0;)
			if ((((magic ^ prev_magic) >> i) & 0xFF) == 0) {
				// Reverse bit i+1 and enforce that bit i+2 differs from it.
				// This may lead to 4-bit (but not longer) sequences of '0' or '1'.
				magic ^= ((magic ^ (magic << 1)) & (4 << i)) ^ (2 << i);
				i -= 6;
			}
		return magic;
	}

	/**
	 * Determines whether matrix with specified parameters needs to be rehashed.
	 */
	static boolean needRehash(int shift, int overall_size, int payload_size, int max_shift) {
		return overall_size > (THRESHOLD_UP >>> shift) ||
			payload_size < (THRESHOLD_DOWN >>> shift) && shift < max_shift;
	}
}
