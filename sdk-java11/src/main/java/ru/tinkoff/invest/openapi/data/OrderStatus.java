package ru.tinkoff.invest.openapi.data;

/**
 * Возможные статусы биржевой заявки.
 */
public enum OrderStatus {
    New, PartiallyFill, Fill, Cancelled, Replaced, PendingCancel, Rejected, PendingReplace, PendingNew
}
