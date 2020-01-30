package ru.tinkoff.invest.openapi.model.orders;

/**
 * Перечисление возможных статусов заявки.
 */
public enum Status {
    New, PartiallyFill, Fill, Cancelled, Replaced, PendingCancel, Rejected, PendingReplace, PendingNew
}
