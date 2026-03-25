# Fractal Block Compression Algorithm


## Introduction

This version focuses in compressing roughly a ppm image using fractals. Fractals for now are referred as linear regressions per channel (RGB) for each pixel read. The process consists in a compression phase and then a decompression.

The image used for now is the basic 256x256 lena image with max 255 value for pixel colors.

## Compression

### 1. Read Image and load Pixels

PPM format is used for now to simplify analysis. Other types are, of course, able to use but they must be mapped to the Pixel class to favor the actual process.

### 2. Split the image in 4x4 range pixel blocks => BxB

### 3. Split the image in 8x8 non superposed domain pixel blocks => 2Bx2B

### 4. For each domain, reduce it to the same dimension of the range blocks

This focuses in transforming the 8x8 domain blocks into 4x4 blocks. How? It get a ratio of reduction, in this case 2. So inside that 8x8 block, inner 2x2 blocks are fetched and averaged for each channel. Then a simple pixel is obtained of those averages. These gets us a 4x4 average block pixels from that 8x8 domain.

### 5. For each range find the best reduced domain block.

For each reduced domain, an error is obtained, following a linear regression approach of least square errors to that range being analyzed. Comparison for each block O(n) is made until the least error block is found. 

One same domain block might be picked for many ranges.

### 6. Save the relation between each range and the best matching domain.

This is being managed by the CompressedBlock class. The range, the domain, the _s_ (gradient or contrast) and the _o_ are also stored to be able to reproduce the new block on decompression iterations.

Note: _s_ and _o_ are saved for each channel!

## Decompression

### Initialization of random noise image.

### For each RangeMatch, a block of 4x4 is reproduced and sent to the new image. This iterated n times.

For each iteration, timestamps are taken, MSE and PSNR is calculated and stored in a benchmark.csv.

Also, each iteration of the image is saved as iter_n.ppm to analyze the progress.
