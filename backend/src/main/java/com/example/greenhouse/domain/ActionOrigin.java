package com.example.greenhouse.domain;

/** Distinguishes between user-triggered and system-triggered audit events. @since 2.1.0 */
public enum ActionOrigin {
  MANUAL,
  AUTOMATIC
}
