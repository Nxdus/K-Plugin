package org.nxdus.realms.paper.Realms.Utils;

import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.loaders.UpdatableLoader;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;

public class SlimeS3Loader extends UpdatableLoader {

    private final String tableName = "realm_slime_world";
    private final String bucketName = "krk-realms-world";
    private final S3Client s3;

    public SlimeS3Loader() {
        String accessKey = "3c445fbf2fc0d31e95625305b9cbf98b";
        String secretKey = "0507cf5aa98189b42f34e1ece712f3cab618d4eaa7227900e6b5970f2696872e";
        String region = "auto";
        String endpoint = "https://6856ad1b1c28fccc4aa7fa2a84b1f0b2.r2.cloudflarestorage.com";

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))  // ใส่ AccessKey และ SecretKey
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    @Override
    public void update() {
        return;
    }

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(worldName + "/world.slimeworld")
                    .build();
            return s3.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new UnknownWorldException(worldName);
        }
    }

    @Override
    public boolean worldExists(String worldName) throws IOException {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(worldName + "/world.slimeworld")
                    .build();
            s3.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public List<String> listWorlds() throws IOException {
        List<String> worldList = new ArrayList<>();
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsV2Response result = s3.listObjectsV2(listObjectsRequest);
            result.contents().forEach(s3Object -> {
                worldList.add(s3Object.key());
            });
        } catch (S3Exception e) {
            throw new IOException(e);
        }
        return worldList;
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {
        try {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(worldName + "/world.slimeworld")
                    .build();
            s3.putObject(putObjectRequest, RequestBody.fromBytes(serializedWorld));
        } catch (S3Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void deleteWorld(String worldName) throws IOException, UnknownWorldException {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(worldName + "/world.slimeworld")
                    .build();
            s3.deleteObject(deleteObjectRequest);

            System.out.println(worldName + "/world.slimeworld has been deleted");
        } catch (NoSuchKeyException e) {
            throw new UnknownWorldException(worldName);
        } catch (S3Exception e) {
            throw new IOException(e);
        }
    }
}