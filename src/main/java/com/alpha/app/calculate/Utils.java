package com.alpha.app.calculate;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.alpha.bean.CalculateBean;
import com.alpha.bean.ScoreBean;

public class Utils {

	private static DBQuery query;

	static {
		query = new DBQuery();
	}

	public static CalculateBean getAddSingle() {
		List<CalculateBean> retList = query.addSingle();

		int maxTimes = retList.stream().max(Comparator.comparingInt(k -> k.getTimes())).get().getTimes();

		List<CalculateBean> newList = retList.stream().filter(p -> p.getTimes() == (maxTimes - 1))
				.collect(Collectors.toList());

		if (newList.size() == 0) {
			newList = retList;
		}

		return getRandom(getNewList(retList));
	}

	public static CalculateBean getMinusSingle() {
		List<CalculateBean> retList = query.minusSingle();

		return getRandom(getNewList(retList));
	}

	public static void updateResult(CalculateBean calcInfo) {
		query.updateResult(calcInfo);
		query.updateHistory(calcInfo);
	}

	public static List<ScoreBean> score() {
		return query.getScores();
	}

	private static List<CalculateBean> getNewList(List<CalculateBean> list) {
		int maxTimes = list.stream().max(Comparator.comparingInt(k -> k.getTimes())).get().getTimes();

		List<CalculateBean> newList = list.stream().filter(p -> p.getTimes() == (maxTimes - 1))
				.collect(Collectors.toList());

		if (newList.size() == 0) {
			newList = list;
		}

		return newList;
	}

	private static <T> T getRandom(List<T> list) {
		if (list.size() == 1) {
			return list.get(0);
		}

		Random r = new Random();
		int nextInt = r.nextInt(list.size() - 1);

		return list.get(nextInt);
	}
}
