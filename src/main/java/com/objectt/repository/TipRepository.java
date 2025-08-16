package com.objectt.repository;

import com.objectt.data.dao.Tip;
import java.util.List;

public interface TipRepository {
    void addTip(String content);
    void deleteTip(int id);
    List<Tip> getAllTips();
    Tip getRandomTip();
    void loadTips();
    void saveTips();
}