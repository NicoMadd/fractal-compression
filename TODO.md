# TODOs

- Adaptive block size. Some images don’t fit fixed 4×4 or 8×8; try different approaches.
- Parametrize mean reduction or bicubic reduction.
- Classify blocks based on their type
    - Sombreado: Sin gradiente significativo
    - Contorno: Gradiente significativo. Representa un cambio de color
    - Rango Medio: Gradiente moderado. 
