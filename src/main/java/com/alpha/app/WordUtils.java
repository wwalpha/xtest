package com.alpha.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.alpha.bean.UpdateBean;
import com.alpha.bean.WordBean;

public class WordUtils {

	private static Integer OFFSET = 7;
	private static Integer DAY_LIMIT = 150;
	private static final int[] INTERVAL = new int[] { 1, 1, 1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 30, 60, 90 };
	private static final Integer TODAY = Integer.valueOf(DateTime.now().toString("yyyyMMdd"));
	private static WordUtils utils;
	private static final String USER_PATH = "/WEB-INF/classes/users/";

	private static Map<String, Properties> userMap = new HashMap<>();

	private WordUtils() {
		ServletContext context = getContext();
		File userFolder = new File(context.getRealPath(USER_PATH));

		for (File file : userFolder.listFiles()) {
			String userName = file.getName().replace(".properties", StringUtils.EMPTY);

			userMap.put(userName, XFileUtils.LoadProperties(file));
		}
	}

	/**
	 * init user's settings
	 * 
	 * @param user
	 */
	private void initSettings(String user) {
		ServletContext context = getContext();
		String userFile = USER_PATH + user + ".properties";

		String filePath = context.getRealPath(userFile);

		Properties prop = XFileUtils.LoadProperties(filePath);

		userMap.put(user, prop);
	}

	static {
		utils = new WordUtils();
	}

	public enum WordType {
		New, Review, Favorite,
	}

	private List<WordBean> getAllList(String userName) {
		List<WordBean> retList = new ArrayList<>();
		List<String> allLines = getAllLines(userName);

		int pos = 1;
		int index = 1;

		for (String line : allLines) {
			if (StringUtils.isEmpty(line)) {
				continue;
			}

			String[] datas = line.split("\\|");

			if (datas.length < 6) {
				continue;
			}

			// only read datas of same username
			if (StringUtils.isNotEmpty(userName) && !StringUtils.equals(userName, datas[0])) {
				continue;
			}

			WordBean bean = new WordBean();
			bean.setUserName(datas[0]);
			bean.setWord(datas[1]);
			bean.setPronounce(datas[2]);
			bean.setVocabulary(datas[3]);

			try {
				bean.setNextTime(Integer.parseInt(datas[4]));
				bean.setTimes(Integer.parseInt(datas[5]));
			} catch (NumberFormatException e) {
				// not use error date
				continue;
			}

			bean.setFavorite(Boolean.parseBoolean(datas[6]));
			bean.setIndex(index++);

			if (datas.length != 7) {
				bean.setSound(datas[7]);
			}

			int rate = bean.getRate();

			if (rate != 0) {
				bean.setStartPos(pos);
				bean.setEndPos(pos + rate - 1);
			} else {
				bean.setStartPos(0);
				bean.setEndPos(0);
			}

			pos += rate;

			retList.add(bean);
		}

		return retList;
	}

	private List<WordBean> getAllList(File file) {
		List<WordBean> retList = new ArrayList<>();
		List<String> allLines = XFileUtils.readLines(file, "UTF-8");

		int index = 1;

		for (String line : allLines) {
			if (StringUtils.isEmpty(line)) {
				continue;
			}

			String[] datas = line.split("\\|");

			if (datas.length < 6) {
				continue;
			}

			WordBean bean = new WordBean();
			bean.setUserName(datas[0]);
			bean.setWord(datas[1]);
			bean.setPronounce(datas[2]);
			bean.setVocabulary(datas[3]);

			try {
				bean.setNextTime(Integer.parseInt(datas[4]));
				bean.setTimes(Integer.parseInt(datas[5]));
			} catch (NumberFormatException e) {
				// not use error date
				continue;
			}

			bean.setFavorite(Boolean.parseBoolean(datas[6]));
			bean.setIndex(index++);

			if (datas.length != 7) {
				bean.setSound(datas[7]);
			}

			retList.add(bean);
		}

		return retList;
	}

	/**
	 * 
	 * @return
	 */
	private List<String> getAllLines(String userName) {
		Properties prop = userMap.get(userName);

		String dbPath = prop.getProperty("DB_FOLDER");
		String dbFile = prop.getProperty("DB_FILE");

		List<String> allLines = new ArrayList<String>();

		String[] files = dbFile.split("|");

		for (String file : files) {
			if (StringUtils.isEmpty(file)) {
				continue;
			}

			String fullPath = Paths.get(dbPath, file).toString();
			File f = new File(fullPath);

			if (!f.exists()) {
				continue;
			}

			try {
				allLines.addAll(FileUtils.readLines(f, "UTF-8"));
			} catch (IOException e) {
			}
		}

		return allLines;
	}

	public static List<String> getUsers() {

		ServletContext context = utils.getContext();
		File userFolder = new File(context.getRealPath(USER_PATH));

		List<String> users = new ArrayList<String>();

		for (File file : userFolder.listFiles()) {
			users.add(file.getName().replace(".properties", StringUtils.EMPTY));
		}

		return users;
	}

	/**
	 * 
	 * @return
	 */
	public static List<WordBean> getNextList(String userName, String type) {
		List<WordBean> retList = new ArrayList<>();
		Set<String> set = new HashSet<>();

		List<WordBean> userList = utils.getUserList(userName, type);

		// not found the user's data
		if (userList.size() == 0) {
			return retList;
		}

		// new words
		if (StringUtils.equals("1", type)) {
			int maxInt = userList.size() > 49 ? 49 : userList.size();

			for (;;) {
				Random r = new Random();
				int nextInt = r.nextInt(maxInt - 1);

				if (!retList.contains(userList.get(nextInt))) {
					retList.add(userList.get(nextInt));
				}

				if (retList.size() == 7) {
					break;
				}
			}
		}

		// review words
		if (StringUtils.equals("2", type)) {
			while (retList.size() < OFFSET) {
				WordBean newWord = utils.getNextWord(userList);

				if (set.contains(newWord.getWord())) {
					continue;
				}

				set.add(newWord.getWord());

				retList.add(newWord);
			}
		}

		return retList;
	}

	/**
	 * Update the study status
	 * 
	 * @param userName
	 * @param list
	 */
	public static void save(String userName, List<UpdateBean> list) {
		Properties props = userMap.get(userName);

		String dbPath = props.getProperty("DB_FOLDER");
		String dbFile = props.getProperty("DB_FILE");
		String[] files = dbFile.split("|");

		// distinct
		List<UpdateBean> newList = utils.distinct(list);

		for (String file : files) {
			if (StringUtils.isEmpty(file)) {
				continue;
			}

			String fullPath = Paths.get(dbPath, file).toString();
			File f = new File(fullPath);

			List<WordBean> allList = utils.getAllList(new File(fullPath));

			for (final UpdateBean bean : newList) {
				// find the word in the file by same user
				WordBean[] result = allList.stream().filter(b -> StringUtils.equals(bean.getWord(), b.getWord())
						&& StringUtils.equals(userName, b.getUserName())).toArray(size -> new WordBean[size]);

				// if can not find user, skip
				if (result.length == 0) {
					continue;
				}

				WordBean target = result[0];

				// update times
				utils.updateTimes(target, bean);
				// update next time
				utils.updateNextTime(target, allList);
			}

			utils.saveFile(f, null);
		}
	}

	private List<UpdateBean> distinct(List<UpdateBean> updateList) {
		Map<String, UpdateBean> map = new HashMap<>();

		for (UpdateBean bean : updateList) {
			if (map.containsKey(bean.getWord())) {
				UpdateBean updateBean = map.get(bean.getWord());

				updateBean.setChecked(updateBean.isChecked() ? true : bean.isChecked());
				updateBean.setFavorite(updateBean.isFavorite() ? true : bean.isFavorite());

				continue;
			}

			map.put(bean.getWord(), bean);
		}

		return new ArrayList<>(map.values());
	}

	public static String download(String userName) {
		List<WordBean> userList = utils.getAllList(userName);

		List<String> allLines = new ArrayList<>();

		userList.stream().forEach(b -> allLines.add(b.toString()));

		return StringUtils.join(allLines, "◆");
	}

	public static String upload(String userName, String allText) {
		List<WordBean> userList = utils.getAllList(userName);

		List<String> allLines = new ArrayList<>();

		userList.stream().forEach(b -> allLines.add(b.toString()));

		return "{file: \"" + StringUtils.join(allLines, "\n") + "\"}";
	}

	/**
	 * update user's setting file
	 * 
	 * @param user
	 * @param file
	 */
	public static boolean updateSettings(MultipartFile file) {
		if (!file.getOriginalFilename().endsWith(".properties")) {
			return false;
		}

		String path = utils.getContext().getRealPath(USER_PATH);

		File settingFile = new File(path + file.getOriginalFilename());

		try {
			file.transferTo(settingFile);
		} catch (IllegalStateException | IOException e) {
		}

		int endIdx = file.getOriginalFilename().lastIndexOf(".properties");
		String user = file.getOriginalFilename().substring(0, endIdx);

		// reinit user's informations
		utils.initSettings(user);

		return true;
	}

	/**
	 * get steam by type
	 * 
	 * @param userName
	 * @param type
	 * @return
	 */
	private List<WordBean> getUserList(String userName, String type) {
		List<WordBean> allList = utils.getAllList(userName);

		if (StringUtils.isEmpty(type)) {
			return allList;
		}

		Stream<WordBean> stream = null;

		switch (type) {
		case "1":
			// new
			long count = allList.stream().filter(p -> p.getTimes() == 0 && p.getNextTime() <= TODAY).count();

			// new words from review
			if (count == 0L) {
				stream = allList.stream().filter(p -> p.getTimes() == 9999);
				break;
			}

			// new words
			stream = getNewwords(allList);

			break;
		case "2":
			// review
			stream = allList.stream().filter(p -> p.getTimes() != 0 && p.getNextTime() <= TODAY);

			break;
		case "3":
			// favorite
			stream = allList.stream().filter(p -> p.isFavorite());

			break;
		default:
			stream = allList.stream().filter(p -> false);
			break;
		}

		return stream.collect(Collectors.toList());
	}

	private Stream<WordBean> getNewwords(List<WordBean> allList) {
		List<WordBean> newList = allList.stream().filter(p -> p.getTimes() == 0 && p.getNextTime() <= TODAY)
				.collect(Collectors.toList());

		long count = newList.stream().map(m -> m.getNextTime()).distinct().count();

		// no news
		if (count == 0) {
			return new ArrayList<WordBean>().stream();
		}

		// one day
		if (count == 1) {
			return newList.stream();
		}

		int lastTime = newList.stream().map(m -> m.getNextTime()).distinct().max((a, b) -> a.compareTo(b)).get()
				.intValue();

		while (true) {
			final int nextTime = lastTime;
			count = newList.stream().filter(p -> p.getNextTime() >= nextTime).count();

			if (count < 49) {
				lastTime -= 1;

				continue;
			}

			List<WordBean> retList = newList.stream().filter(p -> p.getNextTime() >= nextTime)
					.sorted(Comparator.comparingInt(k -> k.getIndex())).collect(Collectors.toList());

			return retList.stream();
		}
	}

	/**
	 * Update the times column
	 * 
	 * @param target
	 * @param bean
	 */
	private void updateTimes(WordBean target, UpdateBean bean) {
		if (bean.isChecked()) {
			target.setTimes(0);

			return;
		}

		target.setTimes(target.getTimes() + 1);
	}

	/**
	 * Update the Next Time Column
	 * 
	 * @param target
	 * @param stream
	 */
	private void updateNextTime(WordBean target, List<WordBean> list) {
		int times = target.getTimes();

		// new word don's have next time
		if (times == 0) {
			MutableDateTime now = MutableDateTime.now();
			now.addDays(1);

			target.setNextTime(Integer.parseInt(now.toString("yyyyMMdd")));
			return;
		}

		int interval = INTERVAL[times - 1];

		Calendar sysTime = Calendar.getInstance();
		// add interval days
		sysTime.add(Calendar.DAY_OF_MONTH, interval);

		DateTime dt = new DateTime(sysTime.getTime());
		int nextTime = Integer.parseInt(dt.toString("yyyyMMdd"));

		// if the times less than 3, set the next time
		if (target.getTimes() < 3) {
			target.setNextTime(nextTime);

			return;
		}

		while (true) {
			final int time = nextTime;

			// count the same day's words
			long count = list.stream().filter(w -> w.getNextTime() == time).count();

			// over the limit of days
			if (count == DAY_LIMIT) {
				nextTime++;

				continue;
			}

			target.setNextTime(nextTime);

			return;
		}
	}

	/**
	 * save the data to file
	 * 
	 * @param stream
	 */
	private void saveFile(File file, List<WordBean> allList) {
		List<String> allLines = new ArrayList<>();

		allList.stream().forEach(b -> allLines.add(b.toString()));

		try {
			FileUtils.writeLines(file, "UTF-8", allLines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get next word of type 2
	 * 
	 * @param allList
	 * @return
	 */
	private WordBean getNextWord(List<WordBean> userList) {
		// get min end pos of same user
		int minPos = 1;
		// get min end pos of same user
		int maxPos = userList.stream().max(Comparator.comparingInt(s -> s.getEndPos())).get().getEndPos();

		while (true) {
			final int nextInt = new Random().nextInt(maxPos - minPos) + minPos;

			List<WordBean> resultList = userList.stream()
					.filter(bean -> bean.getStartPos() <= nextInt && nextInt <= bean.getEndPos())
					.collect(Collectors.toList());

			if (resultList.size() == 0) {
				continue;
			}

			return resultList.get(0);
		}
	}

	/**
	 * 
	 * @return
	 */
	private ServletContext getContext() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getServletContext();
	}

	public static void main(String[] args) {
		// List<WordBean> list = utils.getAllList();
		//
		// list.forEach(bean -> {
		// bean.setUserName("Alpha");
		// });
		//
		// utils.saveFile(list.stream());
	}
}