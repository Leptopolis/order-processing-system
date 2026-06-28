package com.shop.processor;

import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.queue.OrderQueue;
import com.shop.statistics.StatisticsCollector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

