import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  FlatList,
  Alert,
  ActivityIndicator,
  StyleSheet,
  useColorScheme,
  RefreshControl,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getAccountOrders, cancelOrder } from '@/src/services/orderService';
import type { OrderDTO, OrderStatus } from '@/src/types/api';
import { useAuth } from '@/src/context/AuthContext';
import { getThemeColors } from '@/src/constants/colors';

type FilterType = 'all' | 'pending' | 'executed' | 'cancelled' | 'failed';

export default function OrdersSection() {
  const colorScheme = useColorScheme();
  const Colors = getThemeColors(colorScheme);
  const { activeAccount } = useAuth();

  const [orders, setOrders] = useState<OrderDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [filter, setFilter] = useState<FilterType>('all');

  const loadOrders = useCallback(async () => {
    if (!activeAccount) return;

    try {
      setLoading(true);
      const data = await getAccountOrders(activeAccount.accountId);
      setOrders(data || []);
    } catch (error) {
      console.error('Failed to load orders:', error);
      Alert.alert('Error', 'Failed to load orders');
    } finally {
      setLoading(false);
    }
  }, [activeAccount]);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadOrders();
    setRefreshing(false);
  }, [loadOrders]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  const handleCancelOrder = async (orderId: string, stockSymbol: string) => {
    Alert.alert(
      'Cancel Limit Order',
      `Cancel your pending limit order for ${stockSymbol}?`,
      [
        { text: 'No', style: 'cancel' },
        {
          text: 'Yes, Cancel',
          onPress: async () => {
            try {
              await cancelOrder(orderId);
              Alert.alert('Success', 'Order cancelled successfully');
              loadOrders();
            } catch (error: any) {
              Alert.alert('Error', error.message || 'Failed to cancel order');
            }
          },
          style: 'destructive',
        },
      ]
    );
  };

  const filteredOrders = orders.filter(order => {
    if (filter === 'all') return true;
    return order.status.toLowerCase() === filter.toLowerCase();
  });

  const getStatusColor = (status: OrderStatus): string => {
    switch (status) {
      case 'PENDING':
        return '#FFA726';
      case 'EXECUTED':
        return '#66BB6A';
      case 'CANCELLED':
        return '#EF5350';
      case 'FAILED':
        return '#C62828';
      default:
        return '#999';
    }
  };

  const getStatusIcon = (status: OrderStatus): string => {
    switch (status) {
      case 'PENDING':
        return 'clock-outline';
      case 'EXECUTED':
        return 'check-circle';
      case 'CANCELLED':
        return 'cancel';
      case 'FAILED':
        return 'alert-circle';
      default:
        return 'help-circle';
    }
  };

  const renderOrderItem = ({ item }: { item: OrderDTO }) => {
    const statusColor = getStatusColor(item.status);
    const statusIcon = getStatusIcon(item.status);
    const orderTypeLabel = item.orderType === 'BUY_LIMIT' ? 'Buy' : 'Sell';
    const createdDate = new Date(item.createdAt).toLocaleDateString('en-AU');

    return (
      <View style={[styles.orderCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
        <View style={styles.orderHeader}>
          <View style={styles.orderLeft}>
            <Text style={[styles.orderType, { color: Colors.text }]}>
              {orderTypeLabel} {item.quantity}
            </Text>
            <Text style={[styles.orderSymbol, { color: Colors.tint, fontWeight: '700' }]}>
              {item.stockSymbol}
            </Text>
          </View>
          <View style={[styles.statusBadge, { backgroundColor: statusColor + '20' }]}>
            <MaterialCommunityIcons name={statusIcon} size={16} color={statusColor} />
            <Text style={[styles.statusText, { color: statusColor }]}>
              {item.status}
            </Text>
          </View>
        </View>

        <View style={[styles.orderDivider, { backgroundColor: Colors.border }]} />

        <View style={styles.orderDetails}>
          <View style={styles.detailRow}>
            <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.7 }]}>
              Limit Price:
            </Text>
            <Text style={[styles.detailValue, { color: Colors.text }]}>
              A${item.limitPrice.toFixed(2)}
            </Text>
          </View>

          {item.executedAt && (
            <View style={styles.detailRow}>
              <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.7 }]}>
                Executed:
              </Text>
              <Text style={[styles.detailValue, { color: '#66BB6A' }]}>
                {new Date(item.executedAt).toLocaleDateString('en-AU')}
              </Text>
            </View>
          )}

          {item.failureReason && (
            <View style={styles.detailRow}>
              <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.7 }]}>
                Error:
              </Text>
              <Text style={[styles.detailValue, { color: '#C62828' }]}>
                {item.failureReason}
              </Text>
            </View>
          )}

          <View style={styles.detailRow}>
            <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.7 }]}>
              Created:
            </Text>
            <Text style={[styles.detailValue, { color: Colors.text, opacity: 0.6 }]}>
              {createdDate}
            </Text>
          </View>
        </View>

        {item.status === 'PENDING' && (
          <>
            <View style={[styles.orderDivider, { backgroundColor: Colors.border }]} />
            <TouchableOpacity
              style={[styles.cancelButton, { backgroundColor: Colors.background }]}
              onPress={() => handleCancelOrder(item.orderId, item.stockSymbol)}
              activeOpacity={0.7}
            >
              <MaterialCommunityIcons name="trash-can-outline" size={16} color="#EF5350" />
              <Text style={styles.cancelButtonText}>Cancel Order</Text>
            </TouchableOpacity>
          </>
        )}
      </View>
    );
  };

  if (!activeAccount) {
    return (
      <View style={styles.container}>
        <Text style={[styles.emptyText, { color: Colors.text }]}>
          No account selected
        </Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* Filter Tabs */}
      <View style={styles.filterContainer}>
        {(['all', 'pending', 'executed', 'cancelled'] as const).map(f => (
          <TouchableOpacity
            key={f}
            style={[
              styles.filterTab,
              filter === f && [styles.filterTabActive, { backgroundColor: Colors.tint }],
              { borderColor: Colors.border },
            ]}
            onPress={() => setFilter(f)}
            activeOpacity={0.7}
          >
            <Text
              style={[
                styles.filterTabText,
                filter === f
                  ? { color: 'white', fontWeight: '700' }
                  : { color: Colors.text, opacity: 0.6 },
              ]}
            >
              {f.charAt(0).toUpperCase() + f.slice(1)}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Orders List */}
      {loading && !refreshing ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={Colors.tint} />
          <Text style={[styles.loadingText, { color: Colors.text, opacity: 0.6 }]}>
            Loading orders...
          </Text>
        </View>
      ) : (
        <FlatList
          data={filteredOrders}
          renderItem={renderOrderItem}
          keyExtractor={item => item.orderId}
          scrollEnabled={false}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <MaterialCommunityIcons
                name="clipboard-list-outline"
                size={48}
                color={Colors.text}
                style={{ opacity: 0.3, marginBottom: 12 }}
              />
              <Text style={[styles.emptyText, { color: Colors.text }]}>
                No {filter !== 'all' ? filter : ''} orders
              </Text>
              <Text style={[styles.emptySubtext, { color: Colors.text, opacity: 0.6 }]}>
                {filter === 'all'
                  ? 'Your limit orders will appear here'
                  : `No ${filter} orders at this time`}
              </Text>
            </View>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    gap: 12,
  },
  filterContainer: {
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 12,
    marginBottom: 8,
  },
  filterTab: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
    borderWidth: 1,
    alignItems: 'center',
  },
  filterTabActive: {
    borderWidth: 0,
  },
  filterTabText: {
    fontSize: 12,
    fontWeight: '600',
  },
  orderCard: {
    borderWidth: 1,
    borderRadius: 12,
    overflow: 'hidden',
    marginBottom: 10,
  },
  orderHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  orderLeft: {
    flex: 1,
    gap: 4,
  },
  orderType: {
    fontSize: 12,
    fontWeight: '600',
  },
  orderSymbol: {
    fontSize: 16,
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  statusText: {
    fontSize: 11,
    fontWeight: '700',
  },
  orderDivider: {
    height: 1,
  },
  orderDetails: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 8,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  detailLabel: {
    fontSize: 11,
    fontWeight: '600',
  },
  detailValue: {
    fontSize: 12,
    fontWeight: '700',
  },
  cancelButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  cancelButtonText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#EF5350',
  },
  loadingContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 40,
    gap: 12,
  },
  loadingText: {
    fontSize: 12,
    fontWeight: '600',
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 14,
    fontWeight: '700',
    marginBottom: 6,
  },
  emptySubtext: {
    fontSize: 12,
    fontWeight: '500',
  },
});
