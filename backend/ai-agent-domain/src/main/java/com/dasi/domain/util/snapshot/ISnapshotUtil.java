package com.dasi.domain.util.snapshot;

public interface ISnapshotUtil {

    String buildSnapshot(String agentId);

    SnapshotView parseSnapshot(String snapshotRaw);

}
