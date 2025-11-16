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
  Surface,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { theme, spacing } from '../utils/theme';

interface ChatMessage {
  id: string;
  text: string;
  isUser: boolean;
  timestamp: Date;
}

const ChatScreen = ({ navigation }: any) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      text: 'Hello! I\'m your AI health assistant. How can I help you today?',
      isUser: false,
      timestamp: new Date(),
    },
  ]);
  const [inputText, setInputText] = useState('');

  const quickQuestions = [
    "What are normal cycle lengths?",
    "Tell me about ovulation symptoms",
    "When should I see a doctor?",
    "What is a healthy weight?",
  ];

  const handleSendMessage = () => {
    if (!inputText.trim()) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      text: inputText,
      isUser: true,
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText('');

    // Simulate AI response
    setTimeout(() => {
      const aiResponse: ChatMessage = {
        id: (Date.now() + 1).toString(),
        text: "Thank you for your question. I understand you're asking about reproductive health. For personalized advice, please consult with a healthcare provider.",
        isUser: false,
        timestamp: new Date(),
      };
      setMessages(prev => [...prev, aiResponse]);
    }, 1000);
  };

  const handleQuickQuestion = (question: string) => {
    setInputText(question);
    handleSendMessage();
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <IconButton
          icon="arrow-left"
          onPress={() => navigation.goBack()}
        />
        <Title>AI Health Assistant</Title>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView style={styles.messagesContainer}>
        {messages.map((message) => (
          <View
            key={message.id}
            style={[
              styles.messageBubble,
              message.isUser ? styles.userMessage : styles.aiMessage,
            ]}
          >
            <Paragraph style={message.isUser ? styles.userText : styles.aiText}>
              {message.text}
            </Paragraph>
            <Paragraph style={styles.timestamp}>
              {message.timestamp.toLocaleTimeString()}
            </Paragraph>
          </View>
        ))}
      </ScrollView>

      <Surface style={styles.quickQuestionsContainer}>
        <Paragraph style={styles.quickQuestionsTitle}>Quick Questions:</Paragraph>
        <View style={styles.quickQuestions}>
          {quickQuestions.map((question, index) => (
            <Button
              key={index}
              mode="outlined"
              compact
              style={styles.quickQuestionButton}
              onPress={() => handleQuickQuestion(question)}
            >
              {question}
            </Button>
          ))}
        </View>
      </Surface>

      <View style={styles.inputContainer}>
        <TextInput
          mode="outlined"
          placeholder="Type your message..."
          value={inputText}
          onChangeText={setInputText}
          style={styles.input}
          onSubmitEditing={handleSendMessage}
        />
        <IconButton
          icon="send"
          iconColor={theme.colors.primary}
          onPress={handleSendMessage}
          disabled={!inputText.trim()}
        />
      </View>
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
  messagesContainer: {
    flex: 1,
    padding: spacing.md,
  },
  messageBubble: {
    maxWidth: '80%',
    marginBottom: spacing.md,
    padding: spacing.md,
    borderRadius: 16,
  },
  userMessage: {
    alignSelf: 'flex-end',
    backgroundColor: theme.colors.primary,
  },
  aiMessage: {
    alignSelf: 'flex-start',
    backgroundColor: theme.colors.surface,
  },
  userText: {
    color: 'white',
  },
  aiText: {
    color: theme.colors.text,
  },
  timestamp: {
    fontSize: 10,
    marginTop: spacing.xs,
    opacity: 0.7,
  },
  quickQuestionsContainer: {
    padding: spacing.md,
    backgroundColor: theme.colors.surface,
  },
  quickQuestionsTitle: {
    fontSize: 14,
    marginBottom: spacing.sm,
    fontWeight: 'bold',
  },
  quickQuestions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  quickQuestionButton: {
    marginRight: spacing.sm,
    marginBottom: spacing.sm,
  },
  inputContainer: {
    flexDirection: 'row',
    padding: spacing.md,
    alignItems: 'flex-end',
    backgroundColor: theme.colors.surface,
  },
  input: {
    flex: 1,
    marginRight: spacing.sm,
    backgroundColor: theme.colors.background,
  },
});

export default ChatScreen;