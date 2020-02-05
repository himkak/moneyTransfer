package com.revolut.test;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.Test;

import com.revolut.entity.Account;
import com.revolut.entity.AccountState;
import com.revolut.entity.UserDetails;
import com.revolut.repository.AccountRepository;
import com.revolut.util.HibernateUtil;

public class AccountRepositoryTest {

	private final AccountRepository accountRepo = AccountRepository.getInstance();

	@Test
	public void shouldSaveUserAndAccountDetailsInDB_when_userDetailsProvided() {
		UserDetails userDetails = UserDetails.builder().userName("hima").userIdentificationNumber("123").build();
		AccountRepository.getInstance().saveUserAndAccountDetails(userDetails);

		Session session = HibernateUtil.getSessionFactory().openSession();
		Query<UserDetails> query = session.createQuery("from UserDetails where userName=:userName", UserDetails.class);
		query.setParameter("userName", "hima");
		List<UserDetails> usersDetails = query.getResultList();
		Assert.assertEquals(1, usersDetails.size());
	}

	@Test
	public void shouldSaveAccountDetails_when_AccountDetailsProvided() {
		// int accountNum=1;
		int balance = 1;
		UserDetails userDetails = UserDetails.builder().build();
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(userDetails);
		session.getTransaction().commit();
		session.close();
		Account accnt = Account.builder().userDetails(userDetails).balance(balance).state(AccountState.ACTIVE).build();
		accountRepo.saveAccount(accnt);

		session = HibernateUtil.getSessionFactory().openSession();
		Query<Account> query = session.createNativeQuery("select * from Account where USER_ID=:userId", Account.class);
		query.setParameter("userId", userDetails.getUserIdentificationNumber());
		Account origAccnt = query.getSingleResult();
		// assert account num
		Assert.assertNotNull(origAccnt);
		Assert.assertNotNull(origAccnt.getAccountNum());
		Assert.assertEquals(userDetails.getUserIdentificationNumber(),
				origAccnt.getUserDetails().getUserIdentificationNumber());
		Assert.assertEquals(0, origAccnt.getEarmarkedAmt());
		Assert.assertEquals(balance, origAccnt.getBalance());
		session.close();
	}

	@Test(expected=RuntimeException.class)
	public void shouldThrowException_when_AddMoneyCalledForNonExistingAccount() {
		accountRepo.addMoney(10, "1");
	}

	@Test
	public void shouldAddMoneyInSpecificAccntId_when_AddMoneyCalled() {
		int amt=10;
		
		UserDetails userDetails = UserDetails.builder().build();
		Account accnt=Account.builder().userDetails(userDetails).build();
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(userDetails);
		session.save(accnt);
		session.getTransaction().commit();
		session.close();
		
		accountRepo.addMoney(amt, accnt.getAccountNum());
		
		session = HibernateUtil.getSessionFactory().openSession();
		Query<Account> query = session.createNativeQuery("select * from Account where USER_ID=:userId", Account.class);
		query.setParameter("userId", userDetails.getUserIdentificationNumber());
		Account origAccnt = query.getSingleResult();
		session.close();
		Assert.assertEquals(amt, origAccnt.getBalance());
	}

}
