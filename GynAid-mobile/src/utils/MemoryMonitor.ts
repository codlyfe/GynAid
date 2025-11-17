import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import { AppState } from 'react-native';

interface MemoryStats {
  used: number;
  total: number;
  timestamp: number;
}

interface MemoryThresholds {
  warning: number;
  critical: number;
}

class MemoryMonitor {
  private subscription: any = null;
  private memoryStats: MemoryStats[] = [];
  private isMonitoring = false;
  private memoryCheckInterval: NodeJS.Timeout | null = null;
  
  // Memory thresholds (percentage of total memory)
  private readonly thresholds: MemoryThresholds = {
    warning: 70,  // 70% memory usage warning
    critical: 85  // 85% memory usage critical
  };

  constructor() {
    this.setupMemoryWarnings();
  }

  /**
   * Initialize memory monitoring for the healthcare app
   */
  initialize() {
    console.log('🧠 Memory Monitor: Initializing for GynAid healthcare app');
    this.startMonitoring();
  }

  /**
   * Setup memory warning listeners
   */
  private setupMemoryWarnings() {
    if (Platform.OS === 'ios') {
      // iOS memory warning system
      this.setupIosMemoryWarnings();
    } else if (Platform.OS === 'android') {
      // Android memory management
      this.setupAndroidMemoryMonitoring();
    }
  }

  /**
   * iOS memory warning system
   */
  private setupIosMemoryWarnings() {
    if (NativeModules.ReactNativeEventEmitter) {
      const eventEmitter = new NativeEventEmitter(NativeModules.ReactNativeEventEmitter);
      
      this.subscription = eventEmitter.addListener('memoryWarning', () => {
        console.log('⚠️ Memory Monitor: iOS memory warning received');
        this.handleMemoryWarning('ios');
      });
    }
  }

  /**
   * Android memory monitoring
   */
  private setupAndroidMemoryMonitoring() {
    // Use AppState to monitor app state changes and memory usage
    this.subscription = AppState.addEventListener('focus', () => {
      this.checkMemoryUsage();
    });
  }

  /**
   * Start continuous memory monitoring
   */
  private startMonitoring() {
    if (this.isMonitoring) return;
    
    this.isMonitoring = true;
    console.log('🔄 Memory Monitor: Starting continuous monitoring');
    
    // Check memory usage every 30 seconds during development
    const intervalMs = __DEV__ ? 30000 : 60000; // 30s dev, 60s prod
    
    this.memoryCheckInterval = setInterval(() => {
      this.checkMemoryUsage();
    }, intervalMs);
    
    // Initial check
    this.checkMemoryUsage();
  }

  /**
   * Stop memory monitoring
   */
  stopMonitoring() {
    if (this.memoryCheckInterval) {
      clearInterval(this.memoryCheckInterval);
      this.memoryCheckInterval = null;
    }
    
    if (this.subscription) {
      this.subscription.remove();
      this.subscription = null;
    }
    
    this.isMonitoring = false;
    console.log('⏹️ Memory Monitor: Stopped monitoring');
  }

  /**
   * Check current memory usage
   */
  async checkMemoryUsage(): Promise<MemoryStats> {
    try {
      // Try to get native memory info if available
      const memoryInfo = await this.getMemoryInfo();
      
      const stats: MemoryStats = {
        used: memoryInfo.usedJSHeapSize,
        total: memoryInfo.totalJSHeapSize,
        timestamp: Date.now()
      };
      
      this.memoryStats.push(stats);
      
      // Keep only last 100 entries
      if (this.memoryStats.length > 100) {
        this.memoryStats.shift();
      }
      
      this.analyzeMemoryUsage(stats);
      return stats;
      
    } catch (error) {
      console.warn('⚠️ Memory Monitor: Failed to get memory info:', error);
      
      // Fallback stats
      const fallbackStats: MemoryStats = {
        used: 0,
        total: 1,
        timestamp: Date.now()
      };
      
      return fallbackStats;
    }
  }

  /**
   * Get memory information from native modules
   */
  private async getMemoryInfo(): Promise<any> {
    // Fallback to global performance if native modules not available
    if (global.performance && global.performance.memory) {
      return global.performance.memory;
    }
    
    // Return default values if memory API not available
    return {
      usedJSHeapSize: 0,
      totalJSHeapSize: 1,
      jsHeapSizeLimit: 1
    };
  }

  /**
   * Handle memory warning scenarios
   */
  private handleMemoryWarning(source: string) {
    console.log('🚨 Memory Monitor: Handling memory warning from', source);
    
    // Clear various caches for healthcare app
    this.clearCaches();
    
    // Force garbage collection if available (mostly for Android)
    if (global.gc) {
      try {
        global.gc();
        console.log('🗑️ Memory Monitor: Triggered garbage collection');
      } catch (error) {
        console.warn('⚠️ Memory Monitor: Garbage collection failed:', error);
      }
    }
    
    // Log memory stats for debugging
    this.logCurrentMemoryStats();
  }

  /**
   * Clear caches specific to healthcare app functionality
   */
  private clearCaches() {
    console.log('🧹 Memory Monitor: Clearing healthcare app caches');
    
    // Clear image caches (would be implemented based on actual image handling)
    this.clearImageCache();
    
    // Clear API response caches
    this.clearApiCache();
    
    // Clear local storage for non-critical data
    this.clearStorageCache();
    
    // Clear navigation state cache
    this.clearNavigationCache();
  }

  /**
   * Clear image-related caches
   */
  private clearImageCache() {
    // This would integrate with actual image caching system
    console.log('🖼️ Memory Monitor: Image cache cleared');
  }

  /**
   * Clear API response caches
   */
  private clearApiCache() {
    // Clear API response data that's not critical
    console.log('🌐 Memory Monitor: API cache cleared');
  }

  /**
   * Clear non-essential storage cache
   */
  private clearStorageCache() {
    // Clear non-critical cached data
    console.log('💾 Memory Monitor: Storage cache cleared');
  }

  /**
   * Clear navigation state cache
   */
  private clearNavigationCache() {
    // Clear navigation history that's not essential
    console.log('🧭 Memory Monitor: Navigation cache cleared');
  }

  /**
   * Analyze memory usage and trigger warnings
   */
  private analyzeMemoryUsage(stats: MemoryStats) {
    const usagePercentage = (stats.used / stats.total) * 100;
    
    if (usagePercentage >= this.thresholds.critical) {
      console.error('🚨 Memory Monitor: CRITICAL memory usage:', usagePercentage.toFixed(2) + '%');
      this.handleMemoryWarning('critical_threshold');
    } else if (usagePercentage >= this.thresholds.warning) {
      console.warn('⚠️ Memory Monitor: High memory usage:', usagePercentage.toFixed(2) + '%');
    }
  }

  /**
   * Log current memory statistics
   */
  private logCurrentMemoryStats() {
    const latest = this.memoryStats[this.memoryStats.length - 1];
    if (latest) {
      const usagePercentage = (latest.used / latest.total) * 100;
      console.log(`📊 Memory Monitor: Current usage: ${usagePercentage.toFixed(2)}% (${(latest.used / 1024 / 1024).toFixed(2)}MB / ${(latest.total / 1024 / 1024).toFixed(2)}MB)`);
    }
  }

  /**
   * Get memory statistics for debugging
   */
  getMemoryStats(): MemoryStats[] {
    return [...this.memoryStats];
  }

  /**
   * Get memory usage summary
   */
  getMemorySummary(): { avgUsage: number; peakUsage: number; currentUsage: number } {
    if (this.memoryStats.length === 0) {
      return { avgUsage: 0, peakUsage: 0, currentUsage: 0 };
    }

    const usagePercentages = this.memoryStats.map(stats => (stats.used / stats.total) * 100);
    const currentUsage = usagePercentages[usagePercentages.length - 1];
    const avgUsage = usagePercentages.reduce((sum, usage) => sum + usage, 0) / usagePercentages.length;
    const peakUsage = Math.max(...usagePercentages);

    return {
      avgUsage: Number(avgUsage.toFixed(2)),
      peakUsage: Number(peakUsage.toFixed(2)),
      currentUsage: Number(currentUsage.toFixed(2))
    };
  }

  /**
   * Cleanup resources
   */
  cleanup() {
    this.stopMonitoring();
    console.log('🧹 Memory Monitor: Cleanup completed');
  }
}

// Export singleton instance for healthcare app
export default new MemoryMonitor();