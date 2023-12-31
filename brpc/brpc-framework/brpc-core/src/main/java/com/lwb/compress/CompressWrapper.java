package com.lwb.compress;

import com.lwb.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
