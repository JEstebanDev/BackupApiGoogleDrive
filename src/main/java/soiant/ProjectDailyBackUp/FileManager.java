package soiant.ProjectDailyBackUp;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.stereotype.Service;
import com.google.api.client.http.FileContent;
import java.util.Collections;
import java.text.DateFormat;  
import java.util.Date;  
import java.text.SimpleDateFormat;

@Service
public class FileManager {

	private GoogleDriveManager googleDriveManager=new GoogleDriveManager();
	

	public String uploadFile() {
	
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	 	Date date = new Date();
	 	
		try {
			String folderId = getFolderId("BakUpDaily");
			File fileMetadata = new File();
			fileMetadata.setParents(Collections.singletonList(folderId));
			fileMetadata.setName("backup_"+dateFormat.format(date)+".bak");
			
//			Ruta del archivo que sera subido a google drive
			java.io.File filePath = new java.io.File("C:/\\Program Files\\/\\Microsoft SQL Server\\/MSSQL14.MSSQLSERVER/MSSQL/Backup/Daily/DailyBackup.bak");
//			java.io.File filePath = new java.io.File("C:/\\Program Files\\/\\Microsoft SQL Server\\/MSSQL15.MSSQLSERVER/MSSQL/Backup/Backup1.bak");
			
			
			FileContent mediaContent = new FileContent("application/bak",filePath);
			
			googleDriveManager.getInstance().files().create(fileMetadata, mediaContent)
		            .setFields("id")
		            .execute();
			
		} catch (Exception e) {
			System.out.print("Error: "+e);
			return "error";
		}
		return "ok";
	}

	public String getFolderId(String path) throws Exception {
		String parentId = null;
		String[] folderNames = path.split("/");

		Drive driveInstance = googleDriveManager.getInstance();
		for (String name : folderNames) {
			parentId = findOrCreateFolder(parentId, name, driveInstance);
		}
		return parentId;
	}

	private String findOrCreateFolder(String parentId, String folderName, Drive driveInstance) throws Exception {
		String folderId = searchFolderId(parentId, folderName, driveInstance);
		// Folder already exists, so return id
		if (folderId != null) {
			return folderId;
		}
		//Folder dont exists, create it and return folderId
		File fileMetadata = new File();
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		fileMetadata.setName(folderName);

		if (parentId != null) {
			fileMetadata.setParents(Collections.singletonList(parentId));
		}
		return driveInstance.files().create(fileMetadata)
				.setFields("id")
				.execute()
				.getId();
	}

	private String searchFolderId(String parentId, String folderName, Drive service) throws Exception {
		String folderId = null;
		String pageToken = null;
		FileList result = null;

		File fileMetadata = new File();
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		fileMetadata.setName(folderName);

		do {
			String query = " mimeType = 'application/vnd.google-apps.folder' ";
			if (parentId == null) {
				query = query + " and 'root' in parents";
			} else {
				query = query + " and '" + parentId + "' in parents";
			}
			result = service.files().list().setQ(query)
					.setSpaces("drive")
					.setFields("nextPageToken, files(id, name)")
					.setPageToken(pageToken)
					.execute();

			for (File file : result.getFiles()) {
				if (file.getName().equalsIgnoreCase(folderName)) {
					folderId = file.getId();
				}
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null && folderId == null);

		return folderId;
	}
}