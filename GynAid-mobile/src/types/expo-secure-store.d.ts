declare module 'expo-secure-store' {
  export interface SecureStore {
    getItemAsync(key: string): Promise<string | null>;
    deleteItemAsync(key: string): Promise<void>;
  }

  export const SecureStore: SecureStore;
  export default SecureStore;
}