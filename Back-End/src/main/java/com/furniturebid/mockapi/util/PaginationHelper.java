package com.furniturebid.mockapi.util;

import com.furniturebid.mockapi.dto.response.PaginatedResponse;
import com.furniturebid.mockapi.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for paginating lists of items with validation and defaults.
 * Provides a centralized pagination implementation used across all paginated endpoints.
 */
public final class PaginationHelper {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MIN_PAGE = 1;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationHelper() {
        // Utility class, no instantiation
    }

    /**
     * Paginates a list of items with validation and default handling.
     *
     * @param items    the full list of items to paginate
     * @param page     the page number (1-based), or null to use default (1)
     * @param pageSize the number of items per page, or null to use default (20)
     * @param <T>      the type of items
     * @return a PaginatedResponse containing the sublist with pagination metadata
     * @throws ValidationException if page < 1 or pageSize is not in [1, 100]
     */
    public static <T> PaginatedResponse<T> paginate(List<T> items, Integer page, Integer pageSize) {
        int resolvedPage = page != null ? page : DEFAULT_PAGE;
        int resolvedPageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;

        validate(resolvedPage, resolvedPageSize);

        int total = items.size();
        int offset = (resolvedPage - 1) * resolvedPageSize;

        List<T> data;
        if (offset >= total) {
            data = Collections.emptyList();
        } else {
            int end = Math.min(offset + resolvedPageSize, total);
            data = new ArrayList<>(items.subList(offset, end));
        }

        boolean hasMore = (offset + resolvedPageSize) < total;

        return new PaginatedResponse<>(data, total, resolvedPage, resolvedPageSize, hasMore);
    }

    /**
     * Validates page and pageSize parameters.
     *
     * @param page     the page number to validate
     * @param pageSize the page size to validate
     * @throws ValidationException if page < 1 or pageSize is not in [1, 100]
     */
    private static void validate(int page, int pageSize) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (page < MIN_PAGE) {
            fieldErrors.put("page", "Page must be greater than or equal to " + MIN_PAGE);
        }

        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            fieldErrors.put("pageSize", "Page size must be between " + MIN_PAGE_SIZE + " and " + MAX_PAGE_SIZE);
        }

        if (!fieldErrors.isEmpty()) {
            throw new ValidationException("Invalid pagination parameters", fieldErrors);
        }
    }
}
