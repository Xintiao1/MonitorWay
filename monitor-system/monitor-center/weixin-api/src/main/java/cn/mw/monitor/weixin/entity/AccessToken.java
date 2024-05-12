package cn.mw.monitor.weixin.entity;

public class AccessToken {

	private String accessToken;
	private long expireTime;

	@Override
	public String toString() {
		return "AccessToken{" +
				"accessToken='" + accessToken + '\'' +
				", expireTime=" + expireTime +
				'}';
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public AccessToken(String accessToken, String expireIn) {
		super();
		this.accessToken = accessToken;
		expireTime = System.currentTimeMillis()+Integer.parseInt(expireIn)*1000;
	}

	/**
	 * 判断token是否过期
	 * 目前微信access_token有五分钟过渡期 2020/7/1
	 * @return
	 */
	public boolean isExpired() {
		return System.currentTimeMillis()>expireTime;
	}

}
