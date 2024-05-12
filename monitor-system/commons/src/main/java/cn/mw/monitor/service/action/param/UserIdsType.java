package cn.mw.monitor.service.action.param;

import lombok.Data;

import java.util.HashSet;
import java.util.List;

@Data
public class UserIdsType {

	private HashSet<Integer> personUserIds;
	private HashSet<Integer> groupUserIds;
	private HashSet<Integer> orgUserIds;
	private HashSet<Integer> groupIds;
	private List<Integer> emailUserIds;
	private List<Integer> emailGroupUserIds;
}
