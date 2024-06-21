package io.hhplus.tdd.point.domain;

public interface UserPointRepository {
    UserPoint selectById(long id);

    UserPoint insertOrUpdate(long id, long amount);
}