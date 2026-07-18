package com.iwindplus.setup.server.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.service.OssAliyunService;
import com.iwindplus.base.oss.service.OssMinioService;
import com.iwindplus.base.oss.service.OssQiniuService;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
import com.iwindplus.setup.domain.vo.OssConfigVO;
import com.iwindplus.setup.domain.vo.OssTplVO;
import com.iwindplus.setup.server.service.OssConfigService;
import com.iwindplus.setup.server.service.OssService;
import com.iwindplus.setup.server.service.OssTplService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对象存储业务层接口实现类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssAliyunService ossAliyunService;
    private final OssMinioService ossMinioService;
    private final OssQiniuService ossQiniuService;
    private final OssConfigService ossConfigService;
    private final OssTplService ossTplService;
    private final DtpExecutor ossTaskExecutor;

    @Override
    public UploadVO uploadByte(OssUploadByteDTO entity) {
        final OssTplVO ossTpl = this.ossTplService.getByCode(entity.getTplCode());
        final OssConfigVO ossConfig = this.ossConfigService.getDetail(ossTpl.getConfigId());
        final UploadByteDTO attachment = entity.getAttachment();
        if (OssTypeEnum.MINIO.equals(ossConfig.getType())) {
            this.buildOssConfigMinio(ossConfig, ossTpl);
            return this.ossMinioService.uploadFile(ArrayUtil.unWrap(attachment.getData()), entity.getPrefix(), attachment.getSourceFileName(),
                entity.getRenamed(), Boolean.TRUE);
        } else if (OssTypeEnum.ALIYUN.equals(ossConfig.getType())) {
            this.buildOssConfigAliyun(ossConfig, ossTpl);
            return this.ossAliyunService.uploadFile(ArrayUtil.unWrap(attachment.getData()), entity.getPrefix(), attachment.getSourceFileName(),
                entity.getRenamed(), Boolean.TRUE);
        } else if (OssTypeEnum.QINIU.equals(ossConfig.getType())) {
            this.buildOssConfigQiniu(ossConfig, ossTpl);
            return this.ossQiniuService.uploadFile(ArrayUtil.unWrap(attachment.getData()), entity.getPrefix(), attachment.getSourceFileName(),
                entity.getRenamed(), Boolean.TRUE);
        }
        return null;
    }

    @Override
    public void downloadFile(String tplCode, HttpServletResponse response, String relativePath, String fileName) {
        final OssTplVO ossTpl = this.ossTplService.getByCode(tplCode);
        final OssConfigVO ossConfig = this.ossConfigService.getDetail(ossTpl.getConfigId());
        if (OssTypeEnum.MINIO.equals(ossConfig.getType())) {
            this.buildOssConfigMinio(ossConfig, ossTpl);
            this.ossTaskExecutor.execute(() -> this.ossMinioService.downloadFile(response, relativePath, fileName));
        } else if (OssTypeEnum.ALIYUN.equals(ossConfig.getType())) {
            this.buildOssConfigAliyun(ossConfig, ossTpl);
            this.ossTaskExecutor.execute(() -> this.ossAliyunService.downloadFile(response, relativePath, fileName));
        } else if (OssTypeEnum.QINIU.equals(ossConfig.getType())) {
            this.buildOssConfigQiniu(ossConfig, ossTpl);
            this.ossTaskExecutor.execute(() -> this.ossQiniuService.downloadFile(response, relativePath, fileName));
        }
    }

    @Override
    public List<FilePathVO> listSignUrl(String tplCode, List<String> relativePaths, Integer timeout) {
        final OssTplVO ossTpl = this.ossTplService.getByCode(tplCode);
        final OssConfigVO ossConfig = this.ossConfigService.getDetail(ossTpl.getConfigId());
        if (OssTypeEnum.MINIO.equals(ossConfig.getType())) {
            this.buildOssConfigMinio(ossConfig, ossTpl);
            return this.ossMinioService.listSignUrl(relativePaths, timeout, ossTaskExecutor);
        } else if (OssTypeEnum.ALIYUN.equals(ossConfig.getType())) {
            this.buildOssConfigAliyun(ossConfig, ossTpl);
            return this.ossAliyunService.listSignUrl(relativePaths, timeout, ossTaskExecutor);
        } else if (OssTypeEnum.QINIU.equals(ossConfig.getType())) {
            this.buildOssConfigQiniu(ossConfig, ossTpl);
            return this.ossQiniuService.listSignUrl(relativePaths, timeout, ossTaskExecutor);
        }
        return null;
    }

    @Override
    public void removeFiles(String tplCode, List<String> relativePaths) {
        final OssTplVO ossTpl = this.ossTplService.getByCode(tplCode);
        final OssConfigVO ossConfig = this.ossConfigService.getDetail(ossTpl.getConfigId());
        if (OssTypeEnum.MINIO.equals(ossConfig.getType())) {
            this.buildOssConfigMinio(ossConfig, ossTpl);
            this.ossTaskExecutor.execute(() -> this.ossMinioService.removeFiles(relativePaths));
        } else if (OssTypeEnum.ALIYUN.equals(ossConfig.getType())) {
            this.buildOssConfigAliyun(ossConfig, ossTpl);
            this.ossTaskExecutor.execute(() -> this.ossAliyunService.removeFiles(relativePaths));
        } else if (OssTypeEnum.QINIU.equals(ossConfig.getType())) {
            this.buildOssConfigQiniu(ossConfig, ossTpl);
            this.ossTaskExecutor.execute(() -> this.ossQiniuService.removeFiles(relativePaths));
        }
    }

    private void buildOssConfigMinio(OssConfigVO ossConfig, OssTplVO ossTpl) {
        OssProperty.MinioConfig minioConfig = OssProperty.MinioConfig.builder()
            .endpoint(ossConfig.getOssEndpoint())
            .accessKey(ossConfig.getAccessKey())
            .secretKey(ossConfig.getSecretKey())
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .partSize(ossTpl.getPartSize())
            .build();
        OssProperty config = OssProperty.builder()
            .minio(minioConfig)
            .build();
        this.ossMinioService.setConfig(config);
    }

    private void buildOssConfigAliyun(OssConfigVO ossConfig, OssTplVO ossTpl) {
        final StsTokenDTO stsToken = StsTokenDTO.builder()
            .endpoint(ossConfig.getStsEndpoint())
            .roleArn(ossConfig.getRoleArn())
            .policy(ossConfig.getPolicy())
            .build();
        final OssProperty.AliyunConfig aliyunConfig = OssProperty.AliyunConfig.builder()
            .endpoint(ossConfig.getOssEndpoint())
            .accessKey(ossConfig.getAccessKey())
            .secretKey(ossConfig.getSecretKey())
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .partSize(ossTpl.getPartSize())
            .broke(ossTpl.getBroke())
            .sts(stsToken)
            .build();
        OssProperty config = OssProperty.builder()
            .aliyun(aliyunConfig)
            .build();
        this.ossAliyunService.setConfig(config);
    }

    private void buildOssConfigQiniu(OssConfigVO ossConfig, OssTplVO ossTpl) {
        final OssProperty.QiniuConfig qiniuConfig = OssProperty.QiniuConfig.builder()
            .accessKey(ossConfig.getAccessKey())
            .secretKey(ossConfig.getSecretKey())
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .partSize(ossTpl.getPartSize())
            .broke(ossTpl.getBroke())
            .build();
        OssProperty config = OssProperty.builder()
            .qiniu(qiniuConfig)
            .build();
        this.ossQiniuService.setConfig(config);
    }
}
