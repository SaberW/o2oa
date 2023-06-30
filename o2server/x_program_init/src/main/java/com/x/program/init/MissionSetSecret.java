package com.x.program.init;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;

import com.x.base.core.project.config.Config;
import com.x.base.core.project.config.DataServer;
import com.x.base.core.project.tools.H2Tools;
import com.x.program.init.Missions.Mission;

public class MissionSetSecret implements Mission {

	private String secret;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public void execute() {
		try {
			this.changeInternalDataServerPassword(Config.token().getPassword(), getSecret());
			this.changeTokenPassword(getSecret());
			Config.resource_commandQueue().add("ctl -initResourceFactory");
		} catch (Exception e) {
			throw new ExceptionMissionExecute(e);
		}
	}

	private void changeInternalDataServerPassword(String oldPassword, String newPassword)
			throws IOException, URISyntaxException, SQLException {
		org.h2.Driver.load();
		Path path = Config.path_local_repository_data(true).resolve(H2Tools.DATABASE);
		if (Files.exists(path)) {
			try (Connection conn = DriverManager.getConnection("jdbc:h2:" + path.toAbsolutePath().toString(),
					H2Tools.USER, oldPassword)) {
				RunScript.execute(conn,
						new StringReader("ALTER USER " + H2Tools.USER + " SET PASSWORD '" + newPassword + "'"));
			}
		}
	}

	private void changeTokenPassword(String secret) throws Exception {
		Config.token().setPassword(secret);
		Config.token().save();
	}

}
