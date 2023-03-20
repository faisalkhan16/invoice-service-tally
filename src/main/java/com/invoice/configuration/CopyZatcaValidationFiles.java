package com.invoice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Configuration
@Slf4j
public class CopyZatcaValidationFiles {

    @PostConstruct
    public void init(){
        extract("zatcaValidation", "zatcaValidation");
    }

    public void extract(String resourceFolder, String destinationFolder){
            try {
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                        + resourceFolder + "/**");
                URI inJarUri  = new DefaultResourceLoader().getResource("classpath:" + resourceFolder).getURI();

                for (Resource resource : resources){
                    String relativePath = resource
                            .getURI()
                            .getRawSchemeSpecificPart()
                            .replace(inJarUri.getRawSchemeSpecificPart(), "");
                    if (relativePath.isEmpty()){
                        continue;
                    }
                    if (relativePath.endsWith("/") || relativePath.endsWith("\\")) {
                        File dirFile = new File(destinationFolder + relativePath);
                        if (!dirFile.exists()) {
                            dirFile.mkdir();
                        }
                    }
                    else{
                        copyResourceToFilePath(resource, destinationFolder + relativePath);
                    }
                }
            }
            catch (IOException e){
                log.debug("Extraction failed!", e );
            }
    }

    private void copyResourceToFilePath(Resource resource, String filePath) throws IOException{
            InputStream resourceInputStream = resource.getInputStream();
            File file = new File(filePath);
            if (!file.exists()) {
                FileUtils.copyInputStreamToFile(resourceInputStream, file);
            }
     }
}
