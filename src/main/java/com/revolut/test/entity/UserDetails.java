package com.revolut.test.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "User_Details")
public class UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USER_ID")
	private String userIdentificationNumber;

	@Column(name = "USER_NAME", unique = true)
	private String userName;

	@OneToMany(mappedBy="userDetails", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<Account> userAccounts;

	@Override
	public String toString() {
		return "UserDetails [userIdentificationNumber=" + userIdentificationNumber + ", userName=" + userName + "]";
	}

	
}
