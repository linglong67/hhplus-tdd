package io.hhplus.tdd.point.application;

import io.hhplus.tdd.point.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcurrentMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public UserPoint getPoint(long id) {
        validateUser(id);

        return userPointRepository.selectById(id);
    }

    public List<PointHistory> getPointHistories(long id) {
        validateUser(id);

        return pointHistoryRepository.selectAllByUserId(id);
    }

    public UserPoint chargePoint(long id, long amount) {
        Lock lock = getLockForUser(id);
        lock.lock();

        validateUser(id);
        validateAmount(amount);

        UserPoint userPoint = userPointRepository.selectById(id);

        try {
            pointHistoryRepository.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPointRepository.insertOrUpdate(id, userPoint.point() + amount);
        } finally {
            lock.unlock();
            releaseLockForUser(id);
        }
    }

    public UserPoint usePoint(long id, long amount) {
        Lock lock = getLockForUser(id);
        lock.lock();

        validateUser(id);
        validateAmount(amount);

        UserPoint userPoint = userPointRepository.selectById(id);
        if (userPoint.point() < amount) {
            throw new IllegalStateException("잔액을 초과하여 사용할 수 없습니다.");
        }

        try {
            pointHistoryRepository.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

            return userPointRepository.insertOrUpdate(id, userPoint.point() - amount);
        } finally {
            lock.unlock();
            releaseLockForUser(id);
        }
    }

    private void validateUser(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
        }
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("0원 이하로 요청할 수 없습니다.");
        }
    }

    private ReentrantLock getLockForUser(long id) {
        return lockMap.computeIfAbsent(id, k -> new ReentrantLock());
    }

    private void releaseLockForUser(long id) {
        lockMap.computeIfPresent(id, (k, lock) -> lock.hasQueuedThreads() ? lock : null);
    }
}
