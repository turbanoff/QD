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
package com.dxfeed.event.market;

/**
 * Helper SPI-style class to provide middleware with public access to package-private constants and methods.
 */
@SuppressWarnings("UnusedDeclaration")
public class MarketAccessorImpl {
	/*
	 * Design principles:
	 * - single accessor class per package to simplify static import
	 * - public static methods for static import
	 * - method names contain name of corresponding event to avoid name collision and method overload hell
	 * - each flag property has 3 methods: converter from value to flags, getter from flags, and setter to flags
	 * - additional methods can be added
	 */

	private MarketAccessorImpl() {}


	// ========== Order accessor methods ==========

	public static int getOrderFlags(OrderBase order) {
		return order.getFlags();
	}

	public static void setOrderFlags(OrderBase order, int flags) {
		order.setFlags(flags);
	}

	public static int orderExchange(char exchangeCode) {
		Util.checkChar(exchangeCode, OrderBase.EXCHANGE_MASK, "exchangeCode");
		return exchangeCode << OrderBase.EXCHANGE_SHIFT;
	}

	public static char getOrderExchange(int flags) {
		return (char)Util.getBits(flags, OrderBase.EXCHANGE_MASK, OrderBase.EXCHANGE_SHIFT);
	}

	public static int setOrderExchange(int flags, char exchangeCode) {
		Util.checkChar(exchangeCode, OrderBase.EXCHANGE_MASK, "exchangeCode");
		return Util.setBits(flags, OrderBase.EXCHANGE_MASK, OrderBase.EXCHANGE_SHIFT, exchangeCode);
	}

	public static int orderSide(Side side) {
		return side.getCode() << OrderBase.SIDE_SHIFT;
	}

	public static Side getOrderSide(int flags) {
		return Side.valueOf(Util.getBits(flags, OrderBase.SIDE_MASK, OrderBase.SIDE_SHIFT));
	}

	public static int setOrderSide(int flags, Side side) {
		return Util.setBits(flags, OrderBase.SIDE_MASK, OrderBase.SIDE_SHIFT, side.getCode());
	}

	public static int orderScope(Scope scope) {
		return scope.getCode() << OrderBase.SCOPE_SHIFT;
	}

	public static Scope getOrderScope(int flags) {
		return Scope.valueOf(Util.getBits(flags, OrderBase.SCOPE_MASK, OrderBase.SCOPE_SHIFT));
	}

	public static int setScope(int flags, Scope scope) {
		return Util.setBits(flags, OrderBase.SCOPE_MASK, OrderBase.SCOPE_SHIFT, scope.getCode());
	}


	// ========== Profile accessor methods ==========

	public static int getProfileFlags(Profile profile) {
		return profile.getFlags();
	}

	public static void setProfileFlags(Profile profile, int flags) {
		profile.setFlags(flags);
	}

	public static int profileShortSaleRestriction(ShortSaleRestriction restriction) {
		return restriction.getCode() << Profile.SSR_SHIFT;
	}

	public static ShortSaleRestriction getProfileShortSaleRestriction(int flags) {
		return ShortSaleRestriction.valueOf(Util.getBits(flags, Profile.SSR_MASK, Profile.SSR_SHIFT));
	}

	public static int setProfileShortSaleRestriction(int flags, ShortSaleRestriction restriction) {
		return Util.setBits(flags, Profile.SSR_MASK, Profile.SSR_SHIFT, restriction.getCode());
	}

	public static int profileTradingStatus(TradingStatus status) {
		return status.getCode() << Profile.STATUS_SHIFT;
	}

	public static TradingStatus getProfileTradingStatus(int flags) {
		return TradingStatus.valueOf(Util.getBits(flags, Profile.STATUS_MASK, Profile.STATUS_SHIFT));
	}

	public static int setProfileTradingStatus(int flags, TradingStatus status) {
		return Util.setBits(flags, Profile.STATUS_MASK, Profile.STATUS_SHIFT, status.getCode());
	}


	// ========== Summary accessor methods ==========

	public static int getSummaryFlags(Summary summary) {
		return summary.getFlags();
	}

	public static void setSummaryFlags(Summary summary, int flags) {
		summary.setFlags(flags);
	}

	public static int summaryCloseType(PriceType type) {
		return type.getCode() << Summary.DAY_CLOSE_PRICE_TYPE_SHIFT;
	}

	public static PriceType getSummaryCloseType(int flags) {
		return PriceType.valueOf(Util.getBits(flags, Summary.DAY_CLOSE_PRICE_TYPE_MASK, Summary.DAY_CLOSE_PRICE_TYPE_SHIFT));
	}

	public static int setSummaryCloseType(int flags, PriceType type) {
		return Util.setBits(flags, Summary.DAY_CLOSE_PRICE_TYPE_MASK, Summary.DAY_CLOSE_PRICE_TYPE_SHIFT, type.getCode());
	}

	public static int summaryPrevCloseType(PriceType type) {
		return type.getCode() << Summary.PREV_DAY_CLOSE_PRICE_TYPE_SHIFT;
	}

	public static PriceType getSummaryPrevCloseType(int flags) {
		return PriceType.valueOf(Util.getBits(flags, Summary.PREV_DAY_CLOSE_PRICE_TYPE_MASK, Summary.PREV_DAY_CLOSE_PRICE_TYPE_SHIFT));
	}

	public static int setSummaryPrevCloseType(int flags, PriceType type) {
		return Util.setBits(flags, Summary.PREV_DAY_CLOSE_PRICE_TYPE_MASK, Summary.PREV_DAY_CLOSE_PRICE_TYPE_SHIFT, type.getCode());
	}


	// ========== TimeAndSale accessor methods ==========

	public static int getTnsFlags(TimeAndSale tns) {
		return tns.getFlags();
	}

	public static void setTnsFlags(TimeAndSale tns, int flags) {
		tns.setFlags(flags);
	}

	public static int tnsTradeThroughExempt(char tradeThroughExempt) {
		Util.checkChar(tradeThroughExempt, TimeAndSale.TTE_MASK, "tradeThroughExempt");
		return tradeThroughExempt << TimeAndSale.TTE_SHIFT;
	}

	public static char getTnsTradeThroughExempt(int flags ) {
		return (char)Util.getBits(flags, TimeAndSale.TTE_MASK, TimeAndSale.TTE_SHIFT);
	}

	public static int setTnsTradeThroughExempt(int flags, char tradeThroughExempt) {
		Util.checkChar(tradeThroughExempt, TimeAndSale.TTE_MASK, "tradeThroughExempt");
		return Util.setBits(flags, TimeAndSale.TTE_MASK, TimeAndSale.TTE_SHIFT, tradeThroughExempt);
	}

	public static int tnsAggressorSide(Side side) {
		return side.getCode() << TimeAndSale.SIDE_SHIFT;
	}

	public static Side getTnsAggressorSide(int flags) {
		return Side.valueOf(Util.getBits(flags, TimeAndSale.SIDE_MASK, TimeAndSale.SIDE_SHIFT));
	}

	public static int setTnsAggressorSide(int flags, Side side) {
		return Util.setBits(flags, TimeAndSale.SIDE_MASK, TimeAndSale.SIDE_SHIFT, side.getCode());
	}

	public static int tnsSpreadLeg(boolean spreadLeg) {
		return spreadLeg ? TimeAndSale.SPREAD_LEG : 0;
	}

	public static boolean isTnsSpreadLeg(int flags) {
		return (flags & TimeAndSale.SPREAD_LEG) != 0;
	}

	public static int setTnsSpreadLeg(int flags, boolean spreadLeg) {
		return spreadLeg ? flags | TimeAndSale.SPREAD_LEG : flags & ~TimeAndSale.SPREAD_LEG;
	}

	public static int tnsExtendedTradingHours(boolean extendedTradingHours) {
		return extendedTradingHours ? TimeAndSale.ETH : 0;
	}

	public static boolean isTnsExtendedTradingHours(int flags) {
		return (flags & TimeAndSale.ETH) != 0;
	}

	public static int setTnsExtendedTradingHours(int flags, boolean extendedTradingHours) {
		return extendedTradingHours ? flags | TimeAndSale.ETH : flags & ~TimeAndSale.ETH;
	}

	public static int tnsValidTick(boolean validTick) {
		return validTick ? TimeAndSale.VALID_TICK : 0;
	}

	public static boolean isTnsValidTick(int flags) {
		return (flags & TimeAndSale.VALID_TICK) != 0;
	}

	public static int setTnsValidTick(int flags, boolean validTick) {
		return validTick ? flags | TimeAndSale.VALID_TICK : flags & ~TimeAndSale.VALID_TICK;
	}

	public static int tnsType(TimeAndSaleType type) {
		return type.getCode() << TimeAndSale.TYPE_SHIFT;
	}

	public static TimeAndSaleType getTnsType(int flags) {
		return TimeAndSaleType.valueOf(Util.getBits(flags, TimeAndSale.TYPE_MASK, TimeAndSale.TYPE_SHIFT));
	}

	public static int setTnsType(int flags, TimeAndSaleType type) {
		return Util.setBits(flags, TimeAndSale.TYPE_MASK, TimeAndSale.TYPE_SHIFT, type.getCode());
	}

	// ----- Auxiliary methods -----

	/** Returns combination of <code>({@link #tnsNew() tnsNew}() | {@link #tnsValidTick(boolean) tnsValidTick}(validTick))</code>. */
	public static int tnsNewValidTick(boolean validTick) {
		return tnsNew() | tnsValidTick(validTick);
	}

	public static int tnsNew() {
		return tnsType(TimeAndSaleType.NEW);
	}

	public static int tnsCorrection() {
		return tnsType(TimeAndSaleType.CORRECTION);
	}

	public static int tnsCancel() {
		return tnsType(TimeAndSaleType.CANCEL);
	}

	public static boolean isTnsNew(int flags) {
		return getTnsType(flags) == TimeAndSaleType.NEW;
	}

	public static boolean isTnsCorrection(int flags) {
		return getTnsType(flags) == TimeAndSaleType.CORRECTION;
	}

	public static boolean isTnsCancel(int flags) {
		return getTnsType(flags) == TimeAndSaleType.CANCEL;
	}

	/** Clears ValidTick flag and sets type to CORRECTION. */
	public static int correctTns(int flags) {
		return setTnsType(setTnsValidTick(flags, false), TimeAndSaleType.CORRECTION);
	}

	/** Clears ValidTick flag and sets type to CANCEL. */
	public static int cancelTns(int flags) {
		return setTnsType(setTnsValidTick(flags, false), TimeAndSaleType.CANCEL);
	}


	// ========== Trade accessor methods ==========

	public static int getTradeFlags(TradeBase trade) {
		return trade.getFlags();
	}

	public static void setTradeFlags(TradeBase trade, int flags) {
		trade.setFlags(flags);
	}

	public static int tradeTickDirection(Direction direction) {
		return direction.getCode() << TradeBase.DIRECTION_SHIFT;
	}

	public static Direction getTradeTickDirection(int flags) {
		return Direction.valueOf(Util.getBits(flags, TradeBase.DIRECTION_MASK, TradeBase.DIRECTION_SHIFT));
	}

	public static int setTradeTickDirection(int flags, Direction direction) {
		return Util.setBits(flags, TradeBase.DIRECTION_MASK, TradeBase.DIRECTION_SHIFT, direction.getCode());
	}

	public static int tradeExtendedTradingHours(boolean extendedTradingHours) {
		return extendedTradingHours ? TradeBase.ETH : 0;
	}

	public static boolean isTradeExtendedTradingHours(int flags) {
		return (flags & TradeBase.ETH) != 0;
	}

	public static int setTradeExtendedTradingHours(int flags, boolean extendedTradingHours) {
		return extendedTradingHours ? flags | TradeBase.ETH : flags & ~TradeBase.ETH;
	}
}
