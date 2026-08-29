package com.skillspherenexus.notificationservice.enums;

/**
 * Which role(s) a notification is intended for. ALL means every
 * authenticated user sees it in their bell icon feed.
 */
public enum TargetRole {
    ADMINISTRATOR,
    HR_MANAGER,
    EMPLOYEE,
    LEARNER,
    ALL
}
