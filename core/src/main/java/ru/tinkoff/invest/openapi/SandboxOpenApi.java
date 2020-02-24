package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

public interface SandboxOpenApi extends OpenApi {
    @NotNull SandboxContext getSandboxContext();
}
