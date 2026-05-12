<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <!-- Exclude generated code -->
    <Match>
        <Source name="~.*Generated.*"/>
    </Match>

    <!-- Exclude DTOs (records) from serialization warnings -->
    <Match>
        <Bug pattern="SE_BAD_FIELD"/>
        <Class name="~${packageName?replace(".", "\\.")}\..*\.(Request|Response|DTO|Event)$"/>
    </Match>

    <!-- Exclude Spring config classes from utility class detection -->
    <Match>
        <Bug pattern="MS_SHOULD_BE_FINAL"/>
        <Class name="~.*Config$"/>
    </Match>

    <!-- Ignore EI_EXPOSE_REP for entity getters (JPA managed) -->
    <Match>
        <Bug pattern="EI_EXPOSE_REP,EI_EXPOSE_REP2"/>
        <Class name="~${packageName?replace(".", "\\.")}\..*\.domain\..*"/>
    </Match>
</FindBugsFilter>
