// Shared types across all platforms (web, mobile, desktop)

export type UserRole = 'ADMIN' | 'CLIENT' | 'PROVIDER_INDIVIDUAL' | 'PROVIDER_INSTITUTION';

export interface User {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  role: UserRole;
  profileCompletionStatus?: 'INCOMPLETE' | 'BASIC' | 'COMPLETE';
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
  expiresIn: number;
}

// Health-related types
export interface CycleData {
  id?: number;
  userId: number;
  startDate: string;
  endDate?: string;
  flowIntensity: 'LIGHT' | 'NORMAL' | 'HEAVY';
  symptoms: string[];
  mood: string[];
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface HealthCondition {
  id: number;
  name: string;
  category: 'INFERTILITY' | 'ENDOMETRIOSIS' | 'CYCLE_COMPLICATIONS' | 'INFECTIONS' | 'OTHER';
  description: string;
  symptoms: string[];
  treatments: string[];
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface Consultation {
  id: number;
  clientId: number;
  providerId: number;
  providerName?: string;
  type: 'VIDEO' | 'AUDIO' | 'CHAT';
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  scheduledAt: string;
  duration?: number;
  notes?: string;
  prescription?: string;
  followUpRequired?: boolean;
  cost: number;
  paymentStatus: 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
  meetingUrl?: string;
  recordingUrl?: string;
}

export interface Payment {
  id: number;
  consultationId: number;
  amount: number;
  currency: string;
  method: 'MOBILE_MONEY' | 'STRIPE' | 'BANK_TRANSFER';
  provider?: 'MTN' | 'AIRTEL' | 'STRIPE';
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  transactionId?: string;
  phoneNumber?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface HealthTip {
  id: number;
  title: string;
  content: string;
  category: 'NUTRITION' | 'EXERCISE' | 'MENTAL_HEALTH' | 'REPRODUCTIVE_HEALTH' | 'FIRST_AID';
  imageUrl?: string;
  videoUrl?: string;
  isEmergency: boolean;
  tags: string[];
  readTime?: number;
  createdAt: string;
  updatedAt?: string;
}

export interface MOHNotification {
  id: number;
  title: string;
  content: string;
  type: 'ALERT' | 'INFO' | 'WARNING' | 'EMERGENCY';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  videoUrl?: string;
  imageUrl?: string;
  dhisDataSource?: string;
  targetAudience: 'ALL' | 'WOMEN' | 'PREGNANT' | 'ADOLESCENTS';
  region?: string;
  expiresAt?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ChatMessage {
  id: number;
  sessionId: string;
  message: string;
  isBot: boolean;
  timestamp: string;
  audioUrl?: string;
  attachments?: string[];
  metadata?: Record<string, any>;
}

export interface VoiceCommand {
  id: number;
  userId: number;
  command: string;
  response: string;
  audioUrl: string;
  confidence: number;
  timestamp: string;
}

// Provider-related types
export interface Provider {
  id: number;
  userId: number;
  name: string;
  specialty?: string;
  experience?: number;
  rating?: number;
  reviewCount?: number;
  isVerified: boolean;
  isActive: boolean;
  consultationFee?: number;
  bio?: string;
  qualifications: string[];
  languages: string[];
  availableHours?: {
    [key: string]: { start: string; end: string; }[];
  };
}

export type AvailabilityStatus = 'ONLINE' | 'OFFLINE' | 'BUSY' | 'ON_BREAK';
export type ServiceType = 'IMMEDIATE_CONSULTATION' | 'SCHEDULED_VISIT' | 'HOME_VISIT' | 'TELEMEDICINE';

export interface ProviderLocation {
  id: number;
  providerId: number;
  providerName?: string;
  providerEmail?: string;
  latitude?: number;
  longitude?: number;
  address?: string;
  city?: string;
  region?: string;
  availabilityStatus: AvailabilityStatus;
  lastUpdated?: string;
  serviceType?: ServiceType;
  currentActivity?: string;
  accuracy?: number;
  provider?: Provider;
}

// Platform-specific types
export interface PlatformCapabilities {
  isWeb: boolean;
  isMobile: boolean;
  isDesktop: boolean;
  supportsNotifications: boolean;
  supportsLocation: boolean;
  supportsCamera: boolean;
  supportsBiometric: boolean;
  supportsVoice: boolean;
  supportsOffline: boolean;
  supportsFileSystem: boolean;
}
