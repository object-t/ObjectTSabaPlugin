package com.objectt.repository;

import com.objectt.data.dao.UserHome;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Homeデータをローカルへ保存するリポジトリ
 */
public interface HomeRepository {
    /**
     * 指定したプレイヤーのHomeデータを読み込む。
     * データが存在しない場合は新規作成して初期データを返します。
     * @param id playerId
     * @return プレイヤーのHomeデータ
     */
    @NotNull UserHome findById(UUID id);

    /**
     * Homeデータをローカルに保存する。
     * @param obj userHome
     */
    void save(UserHome obj);
}
