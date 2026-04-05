# TODOs

- Adaptive block size. Some images don’t fit fixed 4×4 or 8×8; try different approaches.

- [x] Try with ranges of 8×8 and 16×16

- [x] Use geometric transformations (`TransformationType`, factory, reflections + rotations, encode/decode aligned with `s`/`o`).

- Add metrics on

    - memory usage

    - [x] file compression ratio

- [x] Serialize compression relations and cache it / force recompute (`codebook.fc`, `-c`).

- [x] Bicubic downsampling for domain → range reduction.

- [x] Sample text-style image for runs: `data/images/text-sample.png`, `data/images/pgma/text-sample.ascii.pgm` (512×512 PGMA).
