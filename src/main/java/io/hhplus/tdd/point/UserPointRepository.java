package io.hhplus.tdd.point;

public interface UserPointRepository {
    UserPoint selectById(long id);

    UserPoint insertOrUpdate(long id, long amount);
}