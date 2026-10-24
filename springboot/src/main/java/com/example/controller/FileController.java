package com.example.controller;
import com.example.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.nio.file.*;
import java.io.*;
import java.util.*;
@RestController
@RequestMapping("/files")
public class FileController {
    @Value("${file.storage-path:../files/}") private String storage;
    private Path root() throws IOException {Path root=Paths.get(storage).toAbsolutePath().normalize();Files.createDirectories(root);return root.toRealPath();}
    private String format(Path path) throws IOException {
        try(ImageInputStream stream=ImageIO.createImageInputStream(path.toFile())) {
            if(stream==null)throw new IOException("Invalid image");
            Iterator<ImageReader> readers=ImageIO.getImageReaders(stream);
            if(!readers.hasNext())throw new IOException("Invalid image");
            ImageReader reader=readers.next();
            try {reader.setInput(stream);String name=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!Arrays.asList("jpeg","jpg","png","gif").contains(name) || (long)reader.getWidth(0)*reader.getHeight(0)>20000000)throw new IOException("Invalid image");
                reader.read(0);return name.equals("jpg")?"jpeg":name;
            }finally{reader.dispose();}
        }
    }
    @PostMapping("/upload") public Result upload(@RequestParam MultipartFile file) throws IOException {
        if(file.isEmpty() || file.getSize()>2*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"图片大小应在2MB以内");
        Path directory=root(), temporary=Files.createTempFile(directory,"upload-",".tmp");
        try {
            file.transferTo(temporary);String type;
            try {type=format(temporary);}catch(IOException invalid){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"仅支持有效的 JPG、PNG、GIF 图片");}
            String name=UUID.randomUUID()+"."+(type.equals("jpeg")?"jpg":type);
            Files.move(temporary,directory.resolve(name));return Result.success("/api/files/download/"+name);
        }finally{Files.deleteIfExists(temporary);}
    }
    @GetMapping("/download/{fileName}") public void download(@PathVariable String fileName,HttpServletResponse response) throws IOException {
        if(fileName.contains("/") || fileName.contains("\\") || fileName.contains("..") || !fileName.toLowerCase(Locale.ROOT).matches(".+\\.(jpg|jpeg|png|gif)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"图片不存在");
        Path directory=root(), target=directory.resolve(fileName).normalize();
        if(!Files.isRegularFile(target) || !target.toRealPath().startsWith(directory))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"图片不存在");
        String type;try{type=format(target);}catch(IOException invalid){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"图片不存在");}
        response.setContentType("image/"+type);response.setHeader("X-Content-Type-Options","nosniff");Files.copy(target,response.getOutputStream());
    }
    @GetMapping("/hasDefaultAvatar") public Result defaultAvatar() throws IOException {return Result.success(Files.isRegularFile(root().resolve("defaultAvatar.png")));}
}
