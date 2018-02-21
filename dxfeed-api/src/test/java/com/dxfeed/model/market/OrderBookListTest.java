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
package com.dxfeed.model.market;

import java.util.ArrayList;
import java.util.List;

import com.dxfeed.event.market.*;
import junit.framework.TestCase;

import static com.dxfeed.model.market.CheckedTreeList.Node;

/**
 * Unit test for {@link OrderBookList} class.
 */
public class OrderBookListTest extends TestCase {

	public static final char EXCHANGE = 'Q';
	public static final String MMID = "NYSE";
	public static final String SYMBOL = "IBM";

	private OrderBookList list;

	private Order composite = composite(100);
	private Order regional = regional(101);
	private Order aggregate = aggregate(102);
	private Order order = order(103);

	public OrderBookListTest(String s) {
		super(s);
	}

	@Override
	public void setUp() {
		list = (OrderBookList)new OrderBookModel().getBuyOrders();
	}

	public void testCompositeOnly() {
		list.setFilter(OrderBookModelFilter.COMPOSITE);
		runTestForScope(Scope.COMPOSITE);
	}

	public void testRegionalOnly() {
		list.setFilter(OrderBookModelFilter.REGIONAL);
		runTestForScope(Scope.REGIONAL);
	}

	public void testAggregateOnly() {
		list.setFilter(OrderBookModelFilter.AGGREGATE);
		runTestForScope(Scope.AGGREGATE);
	}

	public void testOrderOnly() {
		list.setFilter(OrderBookModelFilter.ORDER);
		runTestForScope(Scope.ORDER);
	}

	public void testOrderCount() {
		list.setFilter(OrderBookModelFilter.ALL);

		Order order1 = order(104, 4);
		Order order2 = order(105, 5);

		list.insertOrder(aggregate);
		Node<Order> n1 = list.insertOrder(order);
		Node<Order> n2 = list.insertOrder(order1);
		Node<Order> n3 = list.insertOrder(order2);
		assertEquals(3, list.size());

		list.deleteOrderNode(n3);
		assertEquals(2, list.size());

		list.deleteOrderNode(n2);
		assertEquals(1, list.size());
		assertEquals(order, list.get(0));

		list.deleteOrderNode(n1);
		assertEquals(1, list.size());
		assertEquals(aggregate, list.get(0));
	}

	public void testAll() {
		list.setFilter(OrderBookModelFilter.ALL);
		assertEquals(0, list.size());

		List<Node<Order>> nodes = new ArrayList<>();
		nodes.add(list.insertOrder(composite));
		assertEquals(1, list.size());
		assertEquals(composite, list.get(0));

		nodes.add(list.insertOrder(regional));
		assertEquals(1, list.size());
		assertEquals(regional, list.get(0));

		nodes.add(list.insertOrder(aggregate));
		assertEquals(1, list.size());
		assertEquals(aggregate, list.get(0));

		nodes.add(list.insertOrder(order));
		assertEquals(1, list.size());
		assertEquals(order, list.get(0));

		list.deleteOrderNode(nodes.get(3));
		assertEquals(1, list.size());
		assertEquals(aggregate, list.get(0));

		list.deleteOrderNode(nodes.get(2));
		assertEquals(1, list.size());
		assertEquals(regional, list.get(0));

		list.deleteOrderNode(nodes.get(1));
		assertEquals(1, list.size());
		assertEquals(composite, list.get(0));

		list.deleteOrderNode(nodes.get(0));
		assertEquals(0, list.size());
	}

	public void testChangeFilter() {
		list.setFilter(OrderBookModelFilter.ALL);
		assertEquals(0, list.size());

		List<Node<Order>> nodes = new ArrayList<>();
		nodes.add(list.insertOrder(composite));
		nodes.add(list.insertOrder(regional));
		nodes.add(list.insertOrder(aggregate));
		nodes.add(list.insertOrder(order));

		assertEquals(1, list.size());
		assertEquals(order, list.get(0));

		list.setFilter(OrderBookModelFilter.COMPOSITE_REGIONAL_AGGREGATE);
		updateNodes(nodes);
		assertEquals(1, list.size());
		assertEquals(aggregate, list.get(0));

		list.setFilter(OrderBookModelFilter.COMPOSITE_REGIONAL);
		updateNodes(nodes);
		assertEquals(1, list.size());
		assertEquals(regional, list.get(0));

		list.setFilter(OrderBookModelFilter.COMPOSITE);
		updateNodes(nodes);
		assertEquals(1, list.size());
		assertEquals(composite, list.get(0));
	}

	// Utility methods

	protected void runTestForScope(Scope scope) {
		List<Node<Order>> nodes = new ArrayList<>();
		nodes.add(list.insertOrder(composite));
		nodes.add(list.insertOrder(regional));
		nodes.add(list.insertOrder(aggregate));
		nodes.add(list.insertOrder(order));

		assertEquals(1, list.size());
		assertEquals(scope, list.get(0).getScope());

		for (Node<Order> node : nodes) {
			if (node.getValue().getScope() != scope)
				list.deleteOrderNode(node);

			assertEquals(1, list.size());
			assertEquals(scope, list.get(0).getScope());
		}
	}

	protected void updateNodes(List<Node<Order>> nodes) {
		List<Node<Order>> newNodes = new ArrayList<>(nodes.size());
		for (Node<Order> node : nodes)
			newNodes.add(list.updateOrderNode(node));
		nodes.clear();
		nodes.addAll(newNodes);
	}

	protected static Order composite(int price) {
		return createOrder(Scope.COMPOSITE, 0, price, '\0', null);
	}

	protected static Order regional(int price) {
		return createOrder(Scope.REGIONAL, 1, price, EXCHANGE, null);
	}

	protected static Order aggregate(int price) {
		return createOrder(Scope.AGGREGATE, 2, price, EXCHANGE, MMID);
	}

	protected static Order order(int price) {
		return createOrder(Scope.ORDER, 3, price, EXCHANGE, MMID);
	}

	protected static Order order(int price, long index) {
		return createOrder(Scope.ORDER, index, price, EXCHANGE, MMID);
	}

	protected static Order createOrder(Scope scope, long index, int price, char exchange, String mmid) {
		Order order = new Order();

		order.setScope(scope);
		order.setIndex(index);
		order.setOrderSide(Side.BUY);
		order.setPrice(price);
		order.setSize(price);
		order.setExchangeCode(exchange);
		order.setMarketMaker(mmid);
		order.setEventSymbol(SYMBOL);

		return order;
	}
}
