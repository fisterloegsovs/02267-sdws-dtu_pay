package model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import messaging.Event;
import messaging.MessageQueue;

public class AccountService {

	MessageQueue queue;
	public Map<UUID, Customer> customers = new HashMap<>();
	public Map<UUID, Merchant> merchants = new HashMap<>();

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public AccountService(MessageQueue q) {
		this.queue = q;
		queue.addHandler("CustomerRegistrationRequested", this::policyCustomerRegistrationRequested);
		queue.addHandler("CustomerDeregistrationRequested", this::policyCustomerDeregistrationRequested);
		queue.addHandler("CustomerTokensRequested", this::policyCustomerTokensRequested);
		queue.addHandler("GetCustomerAccount", this::policyGetCustomerAccount);

		// Merchant
		queue.addHandler("MerchantRegistrationRequested", this::policyMerchantRegistrationRequested);
		queue.addHandler("MerchantDeregistrationRequested", this::policyMerchantDeregistrationRequested);
		queue.addHandler("GetMerchantAccount", this::policyGetMerchantAccount);
		queue.addHandler("ValidateMerchant", this::policyValidateMerchant);

	}


	
	/* Policies */

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void policyCustomerRegistrationRequested(Event event) {
		var customer = event.getArgument(0, Customer.class);
		createAccount(customer);
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void policyCustomerDeregistrationRequested(Event event) {
		var customerId = event.getArgument(0, UUID.class);
		deregisterCustomer(customerId);
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void policyCustomerTokensRequested(Event event) {
		System.out.println("HELLO");
		var customerId = event.getArgument(0, UUID.class);
		getTokens(customerId);
	}

	/* Commands */

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void createAccount(Customer customer) {
		UUID cid = UUID.randomUUID();

		customers.put(cid, customer);

		queue.publish(new Event("CustomerRegistered", new Object[] {cid}));

		// Create new customer tokens on token manager.
		//queue.publish(new Event("GenerateTokensForCustomer", new Object[]{cid}));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void deregisterCustomer(UUID customerId) {
		boolean deregistered = false;
		if (customers.containsKey(customerId)) {
			customers.remove(customerId);
			deregistered = true;
		}
		queue.publish(new Event("CustomerDeregistered", new Object[]{deregistered}));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public void getTokens(UUID customerId) {
		Integer tokens = 0;
		// TODO
		queue.publish(new Event("CustomerTokenReceived", new Object[] {tokens}));
	}

	/**
	 * @author Asher Sharif (240193)
	 * Policies and Commands for Merchant
	 */
	public void policyMerchantRegistrationRequested(Event event) {
		var merchant = event.getArgument(0, Merchant.class);
		createMerchantAccount(merchant);
	}

	public void policyMerchantDeregistrationRequested(Event event) {
		var merchantId = event.getArgument(0, UUID.class);
		deregisterMerchant(merchantId);
	}

	public void createMerchantAccount(Merchant merchant) {
		UUID mid = UUID.randomUUID();
		merchants.put(mid, merchant);
		queue.publish(new Event("MerchantRegistered", new Object[]{mid}));
	}

	public void deregisterMerchant(UUID merchantId) {
		if (merchants.remove(merchantId) != null) {
			queue.publish(new Event("MerchantDeregistered", new Object[]{true}));
		} else {
			queue.publish(new Event("MerchantDeregistered", new Object[]{false}));
		}
	}

	public void policyValidateMerchant(Event event) {
		UUID merchantId = event.getArgument(0, UUID.class);
		boolean isValid = merchants.containsKey(merchantId);
		queue.publish(new Event("MerchantValidationResponse", new Object[]{isValid}));
	}

	public void policyGetMerchantAccount(Event event) {
		UUID merchantId = event.getArgument(0, UUID.class);
		Merchant merchant = merchants.get(merchantId);
		String merchantAccount = (merchant != null) ? merchant.account() : null;

		queue.publish(new Event("MerchantAccountResponse", new Object[]{merchantAccount}));
	}

	public void policyGetCustomerAccount(Event event) {
		UUID custId = event.getArgument(0, UUID.class);
		Customer customer = customers.get(custId);
		String merchantAccount = (customer != null) ? customer.account() : null;
		queue.publish(new Event("CustomerAccountResponse", new Object[]{merchantAccount}));
	}
}
