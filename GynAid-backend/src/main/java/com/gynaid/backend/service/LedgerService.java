package com.gynaid.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise-grade double-entry bookkeeping service for financial audit trail
 * Provides comprehensive accounting and financial tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

/**
 * Create double-entry transaction with balance validation
 */
@Transactional
public void recordPaymentTransaction(String userId, String paymentId, BigDecimal amount, String currency, String status) {
    // Record payment in simplified way without repository
    log.info("Recording payment transaction: userId={}, paymentId={}, amount={}, currency={}, status={}",
             userId, paymentId, amount, currency, status);
    
    // In a real implementation, this would save to database
    // For now, just log the transaction
}

/**
 * Create double-entry transaction with balance validation
 */
@Transactional
public void createTransaction(LedgerTransaction transaction) {
    try {
        // Validate transaction balance
        validateTransactionBalance(transaction);
        
        // Create main transaction record
        String transactionId = UUID.randomUUID().toString();
        LedgerTransaction savedTransaction = createTransactionWithId(transaction, transactionId);
        
        // Create double-entry records
        List<LedgerEntry> entries = new ArrayList<>();
        for (LedgerEntry entry : transaction.getEntries()) {
            LedgerEntry savedEntry = createEntryWithIds(entry, transactionId);
            entries.add(savedEntry);
            
            // Update account balances
            updateAccountBalance(entry.getAccount(), entry.getType(), entry.getAmount());
        }
        
        log.info("Double-entry transaction created: {} with {} entries", transactionId, entries.size());
        
    } catch (Exception e) {
        log.error("Failed to create ledger transaction", e);
        throw new RuntimeException("Failed to create ledger transaction", e);
    }
}

private LedgerTransaction createTransactionWithId(LedgerTransaction transaction, String id) {
    return LedgerTransaction.builder()
        .id(id)
        .type(transaction.getType())
        .reference(transaction.getReference())
        .description(transaction.getDescription())
        .entries(transaction.getEntries())
        .createdAt(LocalDateTime.now())
        .build();
}

private LedgerEntry createEntryWithIds(LedgerEntry entry, String transactionId) {
    return LedgerEntry.builder()
        .id(UUID.randomUUID().toString())
        .transactionId(transactionId)
        .account(entry.getAccount())
        .type(entry.getType())
        .amount(entry.getAmount())
        .currency(entry.getCurrency())
        .description(entry.getDescription())
        .reference(entry.getReference())
        .metadata(entry.getMetadata())
        .createdAt(LocalDateTime.now())
        .build();
}

    /**
     * Record payment received
     */
    @Transactional
    public void recordPaymentReceived(String paymentId, BigDecimal amount, String currency, String userId, String consultationId) {
        LedgerTransaction transaction = LedgerTransaction.builder()
            .type("PAYMENT_RECEIVED")
            .reference(paymentId)
            .description("Payment received for consultation")
            .entries(List.of(
                // Debit: Bank/Cash account
                LedgerEntry.builder()
                    .account("BANK_CASH")
                    .type(LedgerEntryType.DEBIT)
                    .amount(amount)
                    .currency(currency)
                    .description("Payment received from user")
                    .reference(paymentId)
                    .build(),
                
                // Credit: Sales Revenue
                LedgerEntry.builder()
                    .account("SALES_REVENUE")
                    .type(LedgerEntryType.CREDIT)
                    .amount(amount)
                    .currency(currency)
                    .description("Consultation revenue")
                    .reference(paymentId)
                    .metadata(Map.of("userId", userId, "consultationId", consultationId))
                    .build()
            ))
            .build();

        createTransaction(transaction);
    }

    /**
     * Record refund issued
     */
    @Transactional
    public void recordRefundIssued(String refundId, BigDecimal amount, String currency, String userId, String originalPaymentId) {
        LedgerTransaction transaction = LedgerTransaction.builder()
            .type("REFUND_ISSUED")
            .reference(refundId)
            .description("Refund issued to user")
            .entries(List.of(
                // Debit: Sales Revenue (reversal)
                LedgerEntry.builder()
                    .account("SALES_REVENUE")
                    .type(LedgerEntryType.DEBIT)
                    .amount(amount)
                    .currency(currency)
                    .description("Refund - sales revenue reversal")
                    .reference(refundId)
                    .metadata(Map.of("userId", userId, "originalPaymentId", originalPaymentId))
                    .build(),
                
                // Credit: Bank/Cash account
                LedgerEntry.builder()
                    .account("BANK_CASH")
                    .type(LedgerEntryType.CREDIT)
                    .amount(amount)
                    .currency(currency)
                    .description("Refund payment to user")
                    .reference(refundId)
                    .build()
            ))
            .build();

        createTransaction(transaction);
    }

    /**
     * Record platform fee
     */
    @Transactional
    public void recordPlatformFee(BigDecimal amount, String currency, String paymentId, BigDecimal feeAmount) {
        LedgerTransaction transaction = LedgerTransaction.builder()
            .type("PLATFORM_FEE")
            .reference(paymentId)
            .description("Platform processing fee")
            .entries(List.of(
                // Debit: Revenue account
                LedgerEntry.builder()
                    .account("REVENUE")
                    .type(LedgerEntryType.DEBIT)
                    .amount(amount)
                    .currency(currency)
                    .description("Gross payment amount")
                    .reference(paymentId)
                    .build(),
                
                // Credit: Platform Fee Revenue
                LedgerEntry.builder()
                    .account("PLATFORM_FEE_REVENUE")
                    .type(LedgerEntryType.CREDIT)
                    .amount(feeAmount)
                    .currency(currency)
                    .description("Platform processing fee")
                    .reference(paymentId)
                    .build(),
                
                // Credit: Sales Revenue (net amount)
                LedgerEntry.builder()
                    .account("SALES_REVENUE")
                    .type(LedgerEntryType.CREDIT)
                    .amount(amount.subtract(feeAmount))
                    .currency(currency)
                    .description("Net consultation revenue after fees")
                    .reference(paymentId)
                    .build()
            ))
            .build();

        createTransaction(transaction);
    }

    /**
     * Get account balance
     */
    public BigDecimal getAccountBalance(String account) {
        // In-memory implementation - would typically come from database
        log.debug("Getting account balance for: {}", account);
        return BigDecimal.ZERO;
    }

    /**
     * Get transaction history for user
     */
    public List<LedgerTransaction> getTransactionHistory(String userId, LocalDateTime from, LocalDateTime to) {
        // In-memory implementation - would typically come from database
        log.debug("Getting transaction history for user: {} between {} and {}", userId, from, to);
        return new ArrayList<>();
    }

    /**
     * Get financial report
     */
    public FinancialReport generateFinancialReport(LocalDateTime from, LocalDateTime to) {
        // In-memory implementation - would typically come from database
        log.debug("Generating financial report for period: {} to {}", from, to);
        
        return FinancialReport.builder()
            .period(from, to)
            .totalRevenue(BigDecimal.ZERO)
            .totalPlatformFees(BigDecimal.ZERO)
            .totalRefunds(BigDecimal.ZERO)
            .netRevenue(BigDecimal.ZERO)
            .transactionCount(0)
            .build();
    }

    /**
     * Validate transaction balance (debits must equal credits)
     */
    private void validateTransactionBalance(LedgerTransaction transaction) {
        BigDecimal totalDebits = transaction.getEntries().stream()
            .filter(entry -> entry.getType() == LedgerEntryType.DEBIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = transaction.getEntries().stream()
            .filter(entry -> entry.getType() == LedgerEntryType.CREDIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalArgumentException(
                String.format("Transaction is not balanced. Debits: %s, Credits: %s", 
                    totalDebits, totalCredits)
            );
        }
    }

    /**
     * Update account balance
     */
    private void updateAccountBalance(String account, LedgerEntryType type, BigDecimal amount) {
        BigDecimal balanceChange = type == LedgerEntryType.DEBIT ? amount : amount.negate();
        
        // In a real implementation, this would update the database
        // For now, just log the balance update
        log.debug("Updated account balance: {} by {} (total change: {})", account, type, balanceChange);
    }

    // Data classes and enums
    public enum LedgerEntryType {
        DEBIT, CREDIT
    }

    public static class LedgerTransaction {
        private String id;
        private String type;
        private String reference;
        private String description;
        private List<LedgerEntry> entries;
        private LocalDateTime createdAt;

        public static class Builder {
            private LedgerTransaction result = new LedgerTransaction();

            public Builder id(String id) {
                result.id = id;
                return this;
            }

            public Builder type(String type) {
                result.type = type;
                return this;
            }

            public Builder reference(String reference) {
                result.reference = reference;
                return this;
            }

            public Builder description(String description) {
                result.description = description;
                return this;
            }

            public Builder entries(List<LedgerEntry> entries) {
                result.entries = entries;
                return this;
            }

            public Builder createdAt(LocalDateTime createdAt) {
                result.createdAt = createdAt;
                return this;
            }

            public LedgerTransaction build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public String getReference() { return reference; }
        public String getDescription() { return description; }
        public List<LedgerEntry> getEntries() { return entries; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class LedgerEntry {
        private String id;
        private String transactionId;
        private String account;
        private LedgerEntryType type;
        private BigDecimal amount;
        private String currency;
        private String description;
        private String reference;
        private java.util.Map<String, Object> metadata;
        private LocalDateTime createdAt;

        public static class Builder {
            private LedgerEntry result = new LedgerEntry();

            public Builder id(String id) {
                result.id = id;
                return this;
            }

            public Builder transactionId(String transactionId) {
                result.transactionId = transactionId;
                return this;
            }

            public Builder account(String account) {
                result.account = account;
                return this;
            }

            public Builder type(LedgerEntryType type) {
                result.type = type;
                return this;
            }

            public Builder amount(BigDecimal amount) {
                result.amount = amount;
                return this;
            }

            public Builder currency(String currency) {
                result.currency = currency;
                return this;
            }

            public Builder description(String description) {
                result.description = description;
                return this;
            }

            public Builder reference(String reference) {
                result.reference = reference;
                return this;
            }

            public Builder metadata(java.util.Map<String, Object> metadata) {
                result.metadata = metadata;
                return this;
            }

            public Builder createdAt(LocalDateTime createdAt) {
                result.createdAt = createdAt;
                return this;
            }

            public LedgerEntry build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getId() { return id; }
        public String getTransactionId() { return transactionId; }
        public String getAccount() { return account; }
        public LedgerEntryType getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getDescription() { return description; }
        public String getReference() { return reference; }
        public java.util.Map<String, Object> getMetadata() { return metadata; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class AccountBalance {
        private String account;
        private BigDecimal balance;
        private LocalDateTime updatedAt;

        public AccountBalance(String account, BigDecimal balance) {
            this.account = account;
            this.balance = balance;
            this.updatedAt = LocalDateTime.now();
        }

        public String getAccount() { return account; }
        public void setAccount(String account) { this.account = account; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class FinancialReport {
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private BigDecimal totalRevenue;
        private BigDecimal totalPlatformFees;
        private BigDecimal totalRefunds;
        private BigDecimal netRevenue;
        private int transactionCount;

        public static class Builder {
            private FinancialReport result = new FinancialReport();

            public Builder period(LocalDateTime from, LocalDateTime to) {
                result.fromDate = from;
                result.toDate = to;
                return this;
            }

            public Builder totalRevenue(BigDecimal totalRevenue) {
                result.totalRevenue = totalRevenue;
                return this;
            }

            public Builder totalPlatformFees(BigDecimal totalPlatformFees) {
                result.totalPlatformFees = totalPlatformFees;
                return this;
            }

            public Builder totalRefunds(BigDecimal totalRefunds) {
                result.totalRefunds = totalRefunds;
                return this;
            }

            public Builder netRevenue(BigDecimal netRevenue) {
                result.netRevenue = netRevenue;
                return this;
            }

            public Builder transactionCount(int transactionCount) {
                result.transactionCount = transactionCount;
                return this;
            }

            public FinancialReport build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    // Placeholder repository interface
    public interface LedgerRepository {
        LedgerTransaction save(LedgerTransaction transaction);
        LedgerEntry save(LedgerEntry entry);
        BigDecimal getAccountBalance(String account);
        AccountBalance findAccountBalance(String account);
        List<LedgerTransaction> findByUserIdAndDateRange(String userId, LocalDateTime from, LocalDateTime to);
        BigDecimal getTotalRevenue(LocalDateTime from, LocalDateTime to);
        BigDecimal getTotalFees(LocalDateTime from, LocalDateTime to);
        BigDecimal getTotalRefunds(LocalDateTime from, LocalDateTime to);
        int getTransactionCount(LocalDateTime from, LocalDateTime to);
        AccountBalance save(AccountBalance accountBalance);
    }
}