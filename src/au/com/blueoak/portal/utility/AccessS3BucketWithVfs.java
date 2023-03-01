package au.com.blueoak.portal.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.CreateInvalidationRequest;
import com.amazonaws.services.cloudfront.model.CreateInvalidationResult;
import com.amazonaws.services.cloudfront.model.GetInvalidationRequest;
import com.amazonaws.services.cloudfront.model.InvalidationBatch;
import com.amazonaws.services.cloudfront.model.Paths;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.waiters.WaiterParameters;
import com.amazonaws.waiters.WaiterTimedOutException;
import com.amazonaws.waiters.WaiterUnrecoverableException;
import com.github.vfss3.S3FileSystemOptions;

import au.com.blueoak.portal.BaseTesting;
import au.com.blueoak.portal.ErrorMessageException;

/**
 * {Code snippet to read a file from an S3 bucket via Apache common vfs} <br>
 * <br>
 * <b>(c)2020 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class AccessS3BucketWithVfs implements UtilityPortalTestConstants {

	/** 
	 * Properties object to play with config.properties 
	 * */
	private Properties properties;
	
	/** 
	 * Object to hold aws credential 
	 * */
	private AWSCredentialsProvider awsCredential;
	
	protected static final Log LOG = LogFactory.getLog(BaseTesting.class);
	
	public AccessS3BucketWithVfs(String accessKeyId, String secretAccessKey) {

		try {
			/** Initialize properties object */
			properties = new Properties();
			/** Load properties file through input stream */
			properties.load(new FileInputStream(PORTAL_TEST_SUITE_PROP_FILE));
			/** Set the aws credentials from properties file and these are static in nature so we will use them 
			 * in all the methods of the class
			 */
			System.setProperty("aws.accessKeyId", accessKeyId);
			System.setProperty("aws.secretKey", secretAccessKey);
			awsCredential = new SystemPropertiesCredentialsProvider();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Read a file from S3 bucket then output as a string
	 * 
	 * @param s3BucketName
	 * @param s3PortalFolderName
	 * @param S3FileToGet
	 */
	public String readFileFromS3Bucket(String s3BucketName, String s3PortalFolderName, String S3FileToGet) {
		
		String value = null;
		String BlueBillingS3BucketFilePath = "s3://".concat(s3BucketName).concat(AWS_REGION_S3_PATH)
				.concat("/").concat(s3PortalFolderName).concat("/").concat(S3FileToGet);
		try {
			/** Instantiate S3FileSystemOptions */
			S3FileSystemOptions options = new S3FileSystemOptions();
			/**
			 * Instantiate File System Manager, this is the entry point for vfs, Welcome
			 * vfs!!
			 */
			FileSystemManager fsManager = VFS.getManager();
			/** Need a file object to play around file system object */
			FileObject dir;

			/** Code block to access BlueOak AWS account */
			/** Feed the AWS credentials to File System Options */

			options.setCredentialsProvider(awsCredential);
			// This one would work within the aws account
			dir = fsManager.resolveFile(BlueBillingS3BucketFilePath, options.toFileSystemOptions());

			/** A buffer reader to read file content */
			BufferedReader reader2 = new BufferedReader(new InputStreamReader(dir.getContent().getInputStream()));
			String line2;
			StringBuilder sb2 = new StringBuilder();
			while ((line2 = reader2.readLine()) != null) {
				sb2.append(line2);
			}
			value =  sb2.toString();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	/** 
	 * Use this to read a global language file
	 * */
	public String readGlobalLangFileFromS3(String cloudFrontOrigin, String s3PortalFolderName, String s3FileToRead) {
		
		String contents = null;
		String BlueBillingS3BucketFilePath = "s3://".concat(cloudFrontOrigin)
				.concat(AWS_REGION_S3_PATH).concat("/").concat(s3PortalFolderName).concat("/")
				.concat("assets").concat("/").concat("i18n").concat("/").concat(s3FileToRead);
		try {
			/** Instantiate S3FileSystemOptions */
			S3FileSystemOptions options = new S3FileSystemOptions();
			/**
			 * Instantiate File System Manager, this is the entry point for vfs, Welcome
			 * vfs!!
			 */
			FileSystemManager fsManager = VFS.getManager();
			/** Need a file object to play around file system object */
			FileObject dir;

			/** Code block to access BlueOak AWS account */
			/** Feed the AWS credentials to File System Options */

			options.setCredentialsProvider(awsCredential);
			// This one would work within the aws account
			dir = fsManager.resolveFile(BlueBillingS3BucketFilePath, options.toFileSystemOptions());

			/** A buffer reader to read file content */
			BufferedReader reader2 = new BufferedReader(new InputStreamReader(dir.getContent().getInputStream()));
			String line2;
			StringBuilder sb2 = new StringBuilder();
			while ((line2 = reader2.readLine()) != null) {
				sb2.append(line2);
			}
			contents =  sb2.toString();

		} catch (FileSystemException fe) {

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		StringBuilder stringBuilder = new StringBuilder();
		String message = stringBuilder.append("Read the file '").append(s3FileToRead).append("' from the location '")
				.append(BlueBillingS3BucketFilePath).append("'. The content is:\n").append(contents)
				.toString();
		if (LOG.isDebugEnabled()) {
			LOG.debug(message);
		}
		return contents;
	}

	/** 
	 * Upload files into the S3 bucket portal config
	 * 
	 * */
	public void uploadConfigFileIntoS3Bucket(String localFileToUpload, String s3BucketName, String s3PortalFolderName,
			String s3FileToReplace) throws FileSystemException {

		/** Instantiate S3FileSystemOptions */
		S3FileSystemOptions options = new S3FileSystemOptions();
		/**
		 * Instantiate File System Manager, this is the entry point for vfs, Welcome
		 * vfs!!
		 */
		FileSystemManager fsManager = VFS.getManager();

		/** Code block to access BlueOak AWS account */
		/** Feed the AWS credentials to File System Options */
		options.setCredentialsProvider(awsCredential);
		// This one would work within the aws account
		/** Need a file object to play around file system object */
		String BlueBillingS3BucketFilePath = "s3://".concat(s3BucketName).concat(AWS_REGION_S3_PATH)
				.concat("/").concat(s3PortalFolderName).concat("/").concat(s3FileToReplace);
		FileObject dest = fsManager.resolveFile(BlueBillingS3BucketFilePath);
		FileObject src = fsManager.resolveFile(new File(localFileToUpload).getAbsolutePath());
		dest.copyFrom(src, Selectors.SELECT_SELF);
	}
	
	/**
	 * Use this to upload global language files in the Cloud Front in the specified portal
	 * directory
	 * 
	 * @param localFileToUpload
	 * @param s3BucketName            is the bucket where the portal codes are
	 *                                deployed
	 * @param s3PortalFolderName
	 * @param s3FileToReplaceOrUpload
	 * 
	 * @throws FileSystemException
	 * 
	 */
	public void uploadGlobalLangFiles(String localFileToUpload, String s3BucketName, String s3PortalFolderName,
			String s3FileToReplaceOrUpload) throws FileSystemException {

		/** Instantiate S3FileSystemOptions */
		S3FileSystemOptions options = new S3FileSystemOptions();
		/**
		 * Instantiate File System Manager, this is the entry point for vfs, Welcome
		 * vfs!!
		 */
		FileSystemManager fsManager = VFS.getManager();

		/** Code block to access BlueOak AWS account */
		/** Feed the AWS credentials to File System Options */
		options.setCredentialsProvider(awsCredential);
		// This one would work within the aws account
		/** Need a file object to play around file system object */
		String BlueBillingS3BucketFilePath = "s3://".concat(s3BucketName).concat(AWS_REGION_S3_PATH)
				.concat("/").concat(s3PortalFolderName).concat("/").concat("assets").concat("/").concat("i18n")
				.concat("/").concat(s3FileToReplaceOrUpload);
		FileObject dest = fsManager.resolveFile(BlueBillingS3BucketFilePath);
		FileObject src = fsManager.resolveFile(new File(localFileToUpload).getAbsolutePath());
		dest.copyFrom(src, Selectors.SELECT_SELF);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Successfully uploaded the global language file into this S3 location -> "
					.concat(BlueBillingS3BucketFilePath));
		}
	}
	
	/**
	 * Use this to upload custom language files into instance specific portal config
	 */
	public void uploadCustomLangFiles(String localFileToUpload, String s3BucketName, String s3PortalFolderName,
			String portalName, String langFilesDir, String s3FileToReplace) throws FileSystemException {

		/** Instantiate S3FileSystemOptions */
		S3FileSystemOptions options = new S3FileSystemOptions();
		/**
		 * Instantiate File System Manager, this is the entry point for vfs, Welcome
		 * vfs!!
		 */
		FileSystemManager fsManager = VFS.getManager();

		/** Code block to access BlueOak AWS account */
		/** Feed the AWS credentials to File System Options */
		options.setCredentialsProvider(awsCredential);
		// This one would work within the aws account
		/** Need a file object to play around file system object */
		String BlueBillingS3BucketFilePath = "s3://".concat(s3BucketName).concat(AWS_REGION_S3_PATH)
				.concat("/").concat(s3PortalFolderName).concat("/").concat(portalName).concat(langFilesDir)
				.concat(s3FileToReplace);
		FileObject dest = fsManager.resolveFile(BlueBillingS3BucketFilePath);
		FileObject src = fsManager.resolveFile(new File(localFileToUpload).getAbsolutePath());
		dest.copyFrom(src, Selectors.SELECT_SELF);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Successfully uploaded the custom language file into this S3 location -> "
					.concat(BlueBillingS3BucketFilePath));
		}
	}
	
	/** 
	 * Use this to upload the engine artifacts attachments that would be attached
	 * on the email when an online request is submitted
	 * */
	public void uploadEngineArtifactsForOnlineReqAttachments(String localFileToUpload, String s3BucketName,
			String engineInstanceFolder, String s3FileToReplaceOrUpload) throws FileSystemException {

		/** Instantiate S3FileSystemOptions */
		S3FileSystemOptions options = new S3FileSystemOptions();
		/**
		 * Instantiate File System Manager, this is the entry point for vfs, Welcome
		 * vfs!!
		 */
		FileSystemManager fsManager = VFS.getManager();

		/** Code block to access BlueOak AWS account */
		/** Feed the AWS credentials to File System Options */
		options.setCredentialsProvider(awsCredential);
		// This one would work within the aws account
		/** Need a file object to play around file system object */
		String s3FilePath = "s3://".concat(s3BucketName).concat(AWS_REGION_S3_PATH).concat("/")
				.concat(engineInstanceFolder).concat("/").concat("online-request-artifacts").concat("/")
				.concat("attachments").concat("/").concat(s3FileToReplaceOrUpload);
		FileObject dest = fsManager.resolveFile(s3FilePath);
		FileObject src = fsManager.resolveFile(new File(localFileToUpload).getAbsolutePath());
		dest.copyFrom(src, Selectors.SELECT_SELF);
	}
	
	/** 
	 * Use this to get the objects in the specified bucket.
	 * 
	 * This would only work for buckets in the home directory
	 * 
	 * https://s3.console.aws.amazon.com/s3/home?region=ap-southeast-2
	 * 
	 * */
	public List<String> getObjectIdsInABucket(String bucketName) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Will be getting all the object ID's inside the bucket '".concat(bucketName).concat("'"));
		}
		
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(AWS_REGION_CLIENT_BUILDER).build();
        ListObjectsV2Result result = s3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        List<String> objectIds = new ArrayList<String>();
        if (objects.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("The S3ObjectSummary objects is empty");
			}
		} else {
	        for (S3ObjectSummary object : objects) {
	    			objectIds.add(object.getKey());
	        }
	        
			if (LOG.isDebugEnabled()) {
				LOG.debug("The List<String> to be returned by getObjectIdsInABucket(String) is ".concat(objectIds.toString()));
			}
		}
        
		return objectIds;
	}
	
	/** 
	 * Use this to get the number of items inside an S3 bucket.
	 * 
	 * This would only work for buckets in the home directory
	 * 
	 * https://s3.console.aws.amazon.com/s3/home?region=ap-southeast-2
	 * */
	public int getNumOfObjectsInABucket(String bucketName) {
		
		List<String> objectIds = getObjectIdsInABucket(bucketName);
		int objectIdsSize = objectIds.size();
		String objectIdsSizeStr = Integer.toString(objectIdsSize);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("The int to be returned by getNumOfObjectsInABucket(String) is <".concat(objectIdsSizeStr).concat(">"));
		}
		return objectIdsSize;
	}
	
	/**
	 * Use this to delete specific global language file in the given directory
	 */
	public void deleteGlobalLangFile(String cloudFrontOrigin, String s3PortalFolderName,
			String langFilesDir, String s3FileToReplaceOrUpload) {

		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(AWS_REGION_CLIENT_BUILDER).build();
		String keyPath = s3PortalFolderName.concat("/").concat("assets").concat(langFilesDir)
				.concat(s3FileToReplaceOrUpload);
		s3.deleteObject(cloudFrontOrigin, keyPath);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Successfully deleted the global language file '".concat(s3FileToReplaceOrUpload)
					.concat("' from the S3 directory ").concat(keyPath).concat(" inside the bucket name '")
					.concat(cloudFrontOrigin).concat("'"));
		}
	}
	
	/**
	 * Use this to delete specific custom language file in the given directory
	 */
	public void deleteCustomLangFile(String portalConfigDir, String portalName, String customPortalDir,
			String fileToDelete) {

		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(AWS_REGION_CLIENT_BUILDER).build();
		String keyPath = portalConfigDir.concat("/").concat(portalName).concat(customPortalDir).concat(fileToDelete);
		s3.deleteObject(S3_PORTAL_CONFIG_BUCKET_NAME, keyPath);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Successfully deleted the custom language file '".concat(fileToDelete)
					.concat("' from the S3 directory ").concat(keyPath).concat(" inside the bucket name '")
					.concat(S3_PORTAL_CONFIG_BUCKET_NAME).concat("'"));
		}
	}
	
	/** 
	 * Use this to delete the objects in the development-presign-upload
	 * 
	 * */
	public void deleteAllObjectsInDevPresignUploadBucket() {

		List<String> objectKeysIds = getObjectIdsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME);
		
		if (objectKeysIds.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("The bucket '".concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("' don't have any objects - there's nothing to delete"));
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Will be deleting all the object ID's inside the bucket '"
						.concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("', and it has <")
						.concat(Integer.toString(getNumOfObjectsInABucket(S3_PORTAL_PRESIGN_BUCKET_NAME))
								.concat("> object(s)")));
			}

			final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(AWS_REGION_CLIENT_BUILDER).build();
			try {
				for (String objectKey : objectKeysIds) {
					s3.deleteObject(S3_PORTAL_PRESIGN_BUCKET_NAME, objectKey);
					if (LOG.isDebugEnabled()) {
						LOG.debug("Deleted the object with ID '".concat(objectKey).concat("'"));
					}
				}
			} catch (AmazonServiceException ase) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("AmazonServiceException encountered. See error message for more details -> "
							+ ase.getMessage());
				}
				throw (new ErrorMessageException(
						"AmazonServiceException encountered. See error message for more details -> " + ase.getMessage()));
			}
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Done deleting all objects inside the bucket '".concat(S3_PORTAL_PRESIGN_BUCKET_NAME).concat("'"));
			}
		}
	}
	
	/**
	 * This would create an invalidation request in the specified distributionID in
	 * the cloudfront.
	 * 
	 * You can have more information on this link:
	 * https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/Invalidation.html
	 * 
	 * @param distributionID
	 * @param filesToInvalidate don't pass anything if you want to invalidate the whole thing,
	 *                          but if you want to invalidate certain folders or
	 *                          files you can do pass this '/ASTERISK/assets/i18n/*'
	 *                          or '/ASTERISK/assets/i18n/fil.json'. Note that you
	 *                          should use the actual '*' symbol, not the word
	 *                          ASTERISK
	 */
	public CreateInvalidationResult createInvalidationRequest(String distributionID, boolean waitUntilCompleted,
			String... filesToInvalidate) {
	
		AWSCredentials awsCreds = new DefaultAWSCredentialsProviderChain().getCredentials();
		AmazonCloudFrontClient client = (AmazonCloudFrontClient) AmazonCloudFrontClientBuilder.standard()
				.withRegion(AWS_REGION_CLIENT_BUILDER).withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.build();
		List<String> filesLocation = Arrays.asList(filesToInvalidate);
		int numOfFiles = filesLocation.size();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String referenceId = RandomStringUtils.randomAlphabetic(4).concat(String.valueOf(timestamp.getTime()));

		Paths invalidationPaths;
		if (filesLocation.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Will be invalidating the whole Distribution ID '".concat(distributionID)
						.concat("'. The reference ID is <").concat(referenceId).concat(">"));
			}
			invalidationPaths = new Paths().withItems("/*").withQuantity(1);
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Will be invalidating the following files ".concat(filesLocation.toString())
						.concat(" for Distribution ID '").concat(distributionID)
						.concat("'. The number of items is/are <").concat(String.valueOf(numOfFiles))
						.concat("> while the reference ID is <").concat(referenceId).concat(">"));
			}
			invalidationPaths = new Paths().withItems(filesLocation).withQuantity(numOfFiles);
		}
		InvalidationBatch invalidationBatch = new InvalidationBatch(invalidationPaths, referenceId);
		CreateInvalidationRequest invalidationReq = new CreateInvalidationRequest(distributionID, invalidationBatch);
		CreateInvalidationResult result = client.createInvalidation(invalidationReq);
		
		String invalidationID = result.getInvalidation().getId();
		Date dateCreated = result.getInvalidation().getCreateTime();
		TimeZone utc = TimeZone.getTimeZone("UTC");
		COMPLETE_DATE_FORMAT_WITH_DAY_TIME.setTimeZone(utc);
		String createTime = COMPLETE_DATE_FORMAT_WITH_DAY_TIME.format(dateCreated);
		String message = "Invalidation request has been created. [Date Created: ".concat(createTime)
				.concat(", Invalidation ID: ").concat(invalidationID).concat("]. ");

		if (waitUntilCompleted) {
			LOG.debug(message.concat("Will be waiting until the request is completed"));
			try {
				client.waiters().invalidationCompleted().run(new WaiterParameters<GetInvalidationRequest>(
						new GetInvalidationRequest(distributionID, invalidationID)));
				LOG.debug("The request is completed for Invalidation ID '".concat(invalidationID)
						.concat("' in Distribution ID '").concat(distributionID).concat("'"));
			} catch (AmazonServiceException ase) {
				LOG.debug("AmazonServiceException encountered. See stack trace -> ", ase);
				ase.printStackTrace();
			} catch (WaiterTimedOutException wte) {
				LOG.debug("WaiterTimedOutException encountered. See stack trace -> ", wte);
				wte.printStackTrace();
			} catch (WaiterUnrecoverableException wue) {
				LOG.debug("WaiterUnrecoverableException encountered. See stack trace -> ", wue);
				wue.printStackTrace();
			}
		} else {
			LOG.debug(message.concat("No need to wait until the request is completed"));
		}
		
		return result;
	}
		

}
