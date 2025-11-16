import React, { useState, useEffect } from 'react';
import {
  View,
  ScrollView,
  StyleSheet,
  RefreshControl,
  Dimensions,
} from 'react-native';
import {
  Card,
  Title,
  Paragraph,
  Button,
  Avatar,
  Chip,
  Surface,
  Text,
  IconButton,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../services/AuthService';
import { ApiService } from '../services/ApiService';
import { theme, spacing } from '../utils/theme';

const { width } = Dimensions.get('window');

interface DashboardData {
  nextPeriod: string;
  cycleLength: number;
  fertileWindow: { start: string; end: string };
  upcomingConsultations: any[];
  healthScore: number;
  recentSymptoms: string[];
  cycleData: number[];
}

interface UpcomingConsultation {
  id: number;
  providerName: string;
  scheduledAt: string;
  type: string;
}

const DashboardScreen = ({ navigation }: any) => {
  const { user } = useAuth();
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Try to get cached data first
      const cachedData = await ApiService.getCachedData('dashboard');
      if (cachedData) {
        setDashboardData(cachedData);
      }

      // Fetch fresh data
      const response = await ApiService.get('/api/dashboard');
      const data = response.data;
      
      setDashboardData(data);
      await ApiService.cacheData('dashboard', data);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      
      // Use mock data if API fails
      setDashboardData({
        nextPeriod: '2024-01-25',
        cycleLength: 28,
        fertileWindow: { start: '2024-01-11', end: '2024-01-16' },
        upcomingConsultations: [
          {
            id: 1,
            providerName: 'Dr. Sarah Nakato',
            scheduledAt: '2024-01-15T14:00:00Z',
            type: 'VIDEO',
          },
        ],
        healthScore: 85,
        recentSymptoms: ['Mild cramps', 'Bloating'],
        cycleData: [28, 29, 27, 28, 30, 28],
      });
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const onRefresh = () => {
    setRefreshing(true);
    loadDashboardData();
  };

  const quickActions = [
    {
      title: 'AI Chat',
      icon: 'robot',
      color: theme.colors.primary,
      onPress: () => navigation.navigate('Chat'),
    },
    {
      title: 'Log Cycle',
      icon: 'calendar-plus',
      color: theme.colors.secondary,
      onPress: () => navigation.navigate('Cycle'),
    },
    {
      title: 'Book Consult',
      icon: 'video',
      color: theme.colors.accent,
      onPress: () => navigation.navigate('Consult'),
    },
    {
      title: 'Emergency',
      icon: 'alert',
      color: theme.colors.error,
      onPress: () => navigation.navigate('Emergency'),
    },
  ];

  const chartConfig = {
    backgroundColor: theme.colors.surface,
    backgroundGradientFrom: theme.colors.surface,
    backgroundGradientTo: theme.colors.surface,
    decimalPlaces: 0,
    color: (opacity = 1) => `rgba(233, 30, 99, ${opacity})`,
    labelColor: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
    style: {
      borderRadius: 16,
    },
    propsForDots: {
      r: '6',
      strokeWidth: '2',
      stroke: theme.colors.primary,
    },
  };

  if (loading && !dashboardData) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.loadingContainer}>
          <Text>Loading your health dashboard...</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <View>
            <Title style={styles.welcomeText}>
              Welcome back, {user?.firstName}!
            </Title>
            <Paragraph style={styles.subtitle}>
              Your reproductive health companion
            </Paragraph>
          </View>
          <View style={styles.healthScore}>
            <Avatar.Text
              size={50}
              label={`${dashboardData?.healthScore || 85}%`}
              style={{ backgroundColor: theme.colors.success }}
            />
            <Text style={styles.healthScoreLabel}>Health Score</Text>
          </View>
        </View>

        {/* Quick Actions */}
        <Surface style={styles.quickActionsContainer}>
          <Title style={styles.sectionTitle}>Quick Actions</Title>
          <View style={styles.quickActions}>
            {quickActions.map((action, index) => (
              <Card key={index} style={styles.quickActionCard} onPress={action.onPress}>
                <Card.Content style={styles.quickActionContent}>
                  <IconButton
                    icon={action.icon}
                    size={24}
                    iconColor={action.color}
                  />
                  <Text style={styles.quickActionText}>{action.title}</Text>
                </Card.Content>
              </Card>
            ))}
          </View>
        </Surface>

        {/* Cycle Insights */}
        {dashboardData && (
          <Card style={styles.card}>
            <Card.Content>
              <Title>Cycle Insights</Title>
              <View style={styles.cycleInsights}>
                <View style={styles.insightItem}>
                  <Text style={styles.insightLabel}>Next Period</Text>
                  <Text style={styles.insightValue}>
                    {new Date(dashboardData.nextPeriod).toLocaleDateString()}
                  </Text>
                </View>
                <View style={styles.insightItem}>
                  <Text style={styles.insightLabel}>Cycle Length</Text>
                  <Text style={styles.insightValue}>
                    {dashboardData.cycleLength} days
                  </Text>
                </View>
                <View style={styles.insightItem}>
                  <Text style={styles.insightLabel}>Fertile Window</Text>
                  <Text style={styles.insightValue}>
                    {new Date(dashboardData.fertileWindow.start).toLocaleDateString()} - 
                    {new Date(dashboardData.fertileWindow.end).toLocaleDateString()}
                  </Text>
                </View>
              </View>
              
              {/* Cycle Chart Placeholder */}
              <View style={styles.chartContainer}>
                <Text style={styles.chartTitle}>Cycle Length Trend</Text>
                <Text style={styles.chartPlaceholder}>
                  Chart data available but chart library not installed
                </Text>
                <View style={styles.chartData}>
                  <Text>Recent cycle data: {dashboardData?.cycleData?.join(', ') || 'No data'}</Text>
                </View>
              </View>
            </Card.Content>
          </Card>
        )}

        {/* Upcoming Consultations */}
        {dashboardData && dashboardData.upcomingConsultations && dashboardData.upcomingConsultations.length > 0 && (
          <Card style={styles.card}>
            <Card.Content>
              <Title>Upcoming Consultations</Title>
              {dashboardData.upcomingConsultations.map((consultation: UpcomingConsultation) => (
                <Surface key={consultation.id} style={styles.consultationItem}>
                  <View style={styles.consultationInfo}>
                    <Text style={styles.providerName}>
                      {consultation.providerName}
                    </Text>
                    <Text style={styles.consultationTime}>
                      {new Date(consultation.scheduledAt).toLocaleString()}
                    </Text>
                    <Chip mode="outlined" style={styles.consultationType}>
                      {consultation.type}
                    </Chip>
                  </View>
                  <Button mode="contained" compact>
                    Join
                  </Button>
                </Surface>
              ))}
            </Card.Content>
          </Card>
        )}

        {/* Recent Symptoms */}
        {dashboardData && dashboardData.recentSymptoms && dashboardData.recentSymptoms.length > 0 && (
          <Card style={styles.card}>
            <Card.Content>
              <Title>Recent Symptoms</Title>
              <View style={styles.symptomsContainer}>
                {dashboardData.recentSymptoms.map((symptom: string, index: number) => (
                  <Chip key={index} style={styles.symptomChip}>
                    {symptom}
                  </Chip>
                ))}
              </View>
            </Card.Content>
          </Card>
        )}

        {/* Health Tips */}
        <Card style={styles.card}>
          <Card.Content>
            <Title>Today's Health Tip</Title>
            <Paragraph>
              Stay hydrated! Drinking plenty of water can help reduce bloating 
              and improve overall reproductive health.
            </Paragraph>
            <Button
              mode="outlined"
              onPress={() => navigation.navigate('HealthTips')}
              style={styles.tipButton}
            >
              View More Tips
            </Button>
          </Card.Content>
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  scrollView: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: spacing.md,
    backgroundColor: theme.colors.surface,
  },
  welcomeText: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  subtitle: {
    color: theme.colors.onSurface,
  },
  healthScore: {
    alignItems: 'center',
  },
  healthScoreLabel: {
    fontSize: 12,
    marginTop: spacing.xs,
  },
  quickActionsContainer: {
    margin: spacing.md,
    padding: spacing.md,
    borderRadius: 12,
  },
  sectionTitle: {
    marginBottom: spacing.md,
  },
  quickActions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  quickActionCard: {
    width: '48%',
    marginBottom: spacing.sm,
  },
  quickActionContent: {
    alignItems: 'center',
    paddingVertical: spacing.sm,
  },
  quickActionText: {
    fontSize: 12,
    textAlign: 'center',
    marginTop: spacing.xs,
  },
  card: {
    margin: spacing.md,
    marginTop: 0,
  },
  cycleInsights: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: spacing.md,
  },
  insightItem: {
    alignItems: 'center',
    flex: 1,
  },
  insightLabel: {
    fontSize: 12,
    color: theme.colors.onSurface,
  },
  insightValue: {
    fontSize: 14,
    fontWeight: 'bold',
    marginTop: spacing.xs,
  },
  chartContainer: {
    marginTop: spacing.md,
  },
  chartTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: spacing.sm,
  },
  chartPlaceholder: {
    fontSize: 14,
    color: theme.colors.onSurface,
    marginBottom: spacing.sm,
  },
  chartData: {
    backgroundColor: theme.colors.surface,
    padding: spacing.sm,
    borderRadius: 8,
  },
  chart: {
    borderRadius: 8,
  },
  consultationItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: spacing.md,
    marginVertical: spacing.xs,
    borderRadius: 8,
  },
  consultationInfo: {
    flex: 1,
  },
  providerName: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  consultationTime: {
    fontSize: 14,
    color: theme.colors.onSurface,
    marginVertical: spacing.xs,
  },
  consultationType: {
    alignSelf: 'flex-start',
  },
  symptomsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: spacing.sm,
  },
  symptomChip: {
    marginRight: spacing.sm,
    marginBottom: spacing.sm,
  },
  tipButton: {
    marginTop: spacing.md,
  },
});

export default DashboardScreen;
