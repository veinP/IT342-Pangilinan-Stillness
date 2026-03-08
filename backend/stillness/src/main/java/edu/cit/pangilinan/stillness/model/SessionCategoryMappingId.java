package edu.cit.pangilinan.stillness.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SessionCategoryMappingId implements Serializable {

    private UUID sessionId;
    private UUID categoryId;

    public SessionCategoryMappingId() {}

    public SessionCategoryMappingId(UUID sessionId, UUID categoryId) {
        this.sessionId = sessionId;
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionCategoryMappingId that)) return false;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, categoryId);
    }
}
