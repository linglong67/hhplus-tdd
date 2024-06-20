package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository{

    private final PointHistoryTable pointHistoryTable;

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    @Override
    public PointHistory insert(long userId, long amount, TransactionType type, long updateMillis) {
        return pointHistoryTable.insert(userId, amount, type, updateMillis);
    }
}
