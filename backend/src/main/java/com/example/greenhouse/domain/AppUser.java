package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Authenticated user authorized to operate the greenhouse system. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Email
  @NotBlank
  public String email;

  @NotBlank
  public String fullName;

  @NotBlank
  public String passwordHash;

  @NotBlank
  public String provider = "email";

  @Enumerated(EnumType.STRING)
  public UserRole role = UserRole.VIEWER;

  public boolean verified;

  public String verificationToken;

  public java.time.Instant verificationTokenExpiry;

  public String resetToken;

  public java.time.Instant resetTokenExpiry;
}
