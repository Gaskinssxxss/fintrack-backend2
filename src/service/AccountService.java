package service;

import model.Account;
import persistence.AccountFileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountService {

    private final AccountFileManager fileManager;
    private final List<Account> accounts;

    public AccountService() {
        this.fileManager = new AccountFileManager();
        this.accounts = new ArrayList<>();
        loadAccountsFromFile();
    }

    /**
     * Load accounts from file on initialization
     */
    private void loadAccountsFromFile() {
        List<Account> loaded = fileManager.loadAccounts();
        accounts.addAll(loaded);
    }

    /**
     * Add a new account with auto-generated UUID
     * 
     * @param username the username of the account owner
     * @return the generated account ID
     */
    public synchronized String addAccount(String username, String name, String number, long balance, String type) {
        String id = UUID.randomUUID().toString();
        Account account = new Account(id, username, name, number, balance, type);
        accounts.add(account);
        fileManager.saveAccounts(accounts);
        return id;
    }

    /**
     * Get all accounts
     * 
     * @return list of all accounts
     */
    public synchronized List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    /**
     * Get accounts for a specific user
     * 
     * @param username the username to filter by
     * @return list of accounts belonging to the user
     */
    public synchronized List<Account> getAccountsByUsername(String username) {
        List<Account> userAccounts = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getUsername().equals(username)) {
                userAccounts.add(account);
            }
        }
        return userAccounts;
    }

    /**
     * Update an existing account
     * 
     * @return true if account was found and updated, false otherwise
     */
    public synchronized boolean updateAccount(String id, String username, String name, String number, long balance,
            String type) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId().equals(id)) {
                Account updated = new Account(id, username, name, number, balance, type);
                accounts.set(i, updated);
                fileManager.saveAccounts(accounts);
                return true;
            }
        }
        return false;
    }

    /**
     * Delete an account by ID
     * 
     * @return true if account was found and deleted, false otherwise
     */
    public synchronized boolean deleteAccount(String id) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId().equals(id)) {
                accounts.remove(i);
                fileManager.saveAccounts(accounts);
                return true;
            }
        }
        return false;
    }

    /**
     * Get account by ID
     * 
     * @return the account if found, null otherwise
     */
    public synchronized Account getAccountById(String id) {
        for (Account account : accounts) {
            if (account.getId().equals(id)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Get account by username, name and type
     * 
     * @return the account if found, null otherwise
     */
    public synchronized Account getAccountByDetails(String username, String name, String type) {
        for (Account account : accounts) {
            if (account.getUsername().equals(username)
                    && account.getName().equals(name)
                    && account.getType().equals(type)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Adjust account balance by a delta amount
     * 
     * @param username    the username of the account owner
     * @param accountName the account name
     * @param accountType the account type
     * @param delta       the amount to add (positive) or subtract (negative)
     * @return true if account was found and updated, false otherwise
     */
    public synchronized boolean adjustBalance(String username, String accountName, String accountType, long delta) {
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            if (account.getUsername().equals(username)
                    && account.getName().equals(accountName)
                    && account.getType().equals(accountType)) {

                long newBalance = account.getBalance() + delta;
                Account updated = new Account(
                        account.getId(),
                        account.getUsername(),
                        account.getName(),
                        account.getNumber(),
                        newBalance,
                        account.getType());
                accounts.set(i, updated);
                fileManager.saveAccounts(accounts);
                return true;
            }
        }
        return false;
    }
}
