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
     * @return the generated account ID
     */
    public synchronized String addAccount(String name, String number, long balance, String type) {
        String id = UUID.randomUUID().toString();
        Account account = new Account(id, name, number, balance, type);
        accounts.add(account);
        fileManager.saveAccounts(accounts);
        return id;
    }

    /**
     * Get all accounts
     * @return list of all accounts
     */
    public synchronized List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    /**
     * Update an existing account
     * @return true if account was found and updated, false otherwise
     */
    public synchronized boolean updateAccount(String id, String name, String number, long balance, String type) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId().equals(id)) {
                Account updated = new Account(id, name, number, balance, type);
                accounts.set(i, updated);
                fileManager.saveAccounts(accounts);
                return true;
            }
        }
        return false;
    }

    /**
     * Delete an account by ID
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
}
