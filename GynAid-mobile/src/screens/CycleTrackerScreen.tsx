import React, { useState } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  Alert,
} from 'react-native';
import {
  Title,
  Paragraph,
  Card,
  Button,
  IconButton,
  TextInput,
  Chip,
  Surface,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { theme, spacing } from '../utils/theme';

interface CycleEntry {
  date: string;
  flow: 'light' | 'medium' | 'heavy' | null;
  symptoms: string[];
  notes: string;
}

const CycleTrackerScreen = ({ navigation }: any) => {
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [flow, setFlow] = useState<'light' | 'medium' | 'heavy' | null>(null);
  const [symptoms, setSymptoms] = useState<string[]>([]);
  const [notes, setNotes] = useState('');
  const [cycleEntries, setCycleEntries] = useState<CycleEntry[]>([]);

  const availableSymptoms = [
    'Cramping', 'Headache', 'Bloating', 'Mood swings',
    'Breast tenderness', 'Fatigue', 'Back pain', 'Nausea'
  ];

  const flowOptions = [
    { value: 'light', label: 'Light', color: '#FFE0E0' },
    { value: 'medium', label: 'Medium', color: '#FF8A80' },
    { value: 'heavy', label: 'Heavy', color: '#D32F2F' },
  ];

  const toggleSymptom = (symptom: string) => {
    setSymptoms(prev =>
      prev.includes(symptom)
        ? prev.filter(s => s !== symptom)
        : [...prev, symptom]
    );
  };

  const handleSaveEntry = () => {
    const newEntry: CycleEntry = {
      date: selectedDate,
      flow,
      symptoms,
      notes,
    };

    setCycleEntries(prev => [...prev, newEntry]);
    
    // Reset form
    setFlow(null);
    setSymptoms([]);
    setNotes('');
    
    Alert.alert('Success', 'Cycle entry saved successfully!');
  };

  const getNextPeriodPrediction = () => {
    if (cycleEntries.length >= 3) {
      const recentCycles = cycleEntries.slice(-3);
      // Simple calculation - in real app, this would be more sophisticated
      return 28; // Default 28 days
    }
    return null;
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <IconButton
          icon="arrow-left"
          onPress={() => navigation.goBack()}
        />
        <Title>Cycle Tracker</Title>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView style={styles.content}>
        {/* Prediction Card */}
        <Card style={styles.card}>
          <Card.Content>
            <Title>Next Period Prediction</Title>
            {getNextPeriodPrediction() ? (
              <View style={styles.prediction}>
                <Paragraph style={styles.predictionText}>
                  Expected in approximately {getNextPeriodPrediction()} days
                </Paragraph>
              </View>
            ) : (
              <Paragraph style={styles.noDataText}>
                Track for at least 3 cycles to get predictions
              </Paragraph>
            )}
          </Card.Content>
        </Card>

        {/* Current Day Tracker */}
        <Card style={styles.card}>
          <Card.Content>
            <Title>Today's Entry</Title>
            
            <TextInput
              label="Date"
              value={selectedDate}
              onChangeText={setSelectedDate}
              mode="outlined"
              style={styles.input}
              disabled
            />

            <Paragraph style={styles.label}>Flow Intensity:</Paragraph>
            <View style={styles.flowButtons}>
              {flowOptions.map(option => (
                <Button
                  key={option.value}
                  mode={flow === option.value ? 'contained' : 'outlined'}
                  onPress={() => setFlow(option.value as any)}
                  style={[
                    styles.flowButton,
                    flow === option.value && { backgroundColor: option.color }
                  ]}
                >
                  {option.label}
                </Button>
              ))}
            </View>

            <Paragraph style={styles.label}>Symptoms:</Paragraph>
            <View style={styles.symptomsContainer}>
              {availableSymptoms.map(symptom => (
                <Chip
                  key={symptom}
                  selected={symptoms.includes(symptom)}
                  onPress={() => toggleSymptom(symptom)}
                  style={styles.symptomChip}
                >
                  {symptom}
                </Chip>
              ))}
            </View>

            <TextInput
              label="Notes (optional)"
              value={notes}
              onChangeText={setNotes}
              mode="outlined"
              multiline
              numberOfLines={3}
              style={styles.input}
            />

            <Button
              mode="contained"
              onPress={handleSaveEntry}
              style={styles.saveButton}
            >
              Save Entry
            </Button>
          </Card.Content>
        </Card>

        {/* Recent Entries */}
        {cycleEntries.length > 0 && (
          <Card style={styles.card}>
            <Card.Content>
              <Title>Recent Entries</Title>
              {cycleEntries.slice(-5).map((entry, index) => (
                <Surface key={index} style={styles.entrySurface}>
                  <Paragraph style={styles.entryDate}>
                    {new Date(entry.date).toLocaleDateString()}
                  </Paragraph>
                  <Paragraph style={styles.entryDetails}>
                    {entry.flow ? `Flow: ${entry.flow}` : 'No flow'}
                    {entry.symptoms.length > 0 && ` • Symptoms: ${entry.symptoms.join(', ')}`}
                  </Paragraph>
                </Surface>
              ))}
            </Card.Content>
          </Card>
        )}
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: spacing.md,
    backgroundColor: theme.colors.surface,
  },
  content: {
    flex: 1,
    padding: spacing.md,
  },
  card: {
    marginBottom: spacing.md,
  },
  input: {
    marginBottom: spacing.md,
    backgroundColor: theme.colors.background,
  },
  label: {
    marginBottom: spacing.sm,
    color: theme.colors.onSurface,
    fontWeight: 'bold',
  },
  flowButtons: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: spacing.md,
  },
  flowButton: {
    flex: 1,
    marginHorizontal: spacing.xs,
  },
  symptomsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: spacing.md,
  },
  symptomChip: {
    marginRight: spacing.sm,
    marginBottom: spacing.sm,
  },
  saveButton: {
    marginTop: spacing.md,
  },
  prediction: {
    alignItems: 'center',
    padding: spacing.md,
    backgroundColor: theme.colors.surface,
    borderRadius: 8,
    marginTop: spacing.sm,
  },
  predictionText: {
    fontSize: 16,
    color: theme.colors.primary,
    fontWeight: 'bold',
  },
  noDataText: {
    textAlign: 'center',
    color: theme.colors.onSurface,
    marginTop: spacing.sm,
  },
  entrySurface: {
    padding: spacing.md,
    marginBottom: spacing.sm,
    borderRadius: 8,
  },
  entryDate: {
    fontWeight: 'bold',
    marginBottom: spacing.xs,
  },
  entryDetails: {
    fontSize: 14,
    color: theme.colors.onSurface,
  },
});

export default CycleTrackerScreen;