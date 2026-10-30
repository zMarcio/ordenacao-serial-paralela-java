# Regras do projeto

1. **Linguagem: Java** (somente Java puro, sem bibliotecas externas, sem Maven/Gradle).
   Compila com `javac` e roda com `java`. Nada mais.
2. **Código o mais "burro" possível de entender.**
   - Nomes de variáveis e métodos em português e bem óbvios (`tamanhoDoVetor`, `ordenarSerial`).
   - Um arquivo = uma ideia (um algoritmo por arquivo).
   - Nada de truques: sem streams encadeadas, sem lambdas "espertas", sem genéricos complicados.
   - Laços `for` simples, `if` simples. Se precisar escolher entre curto e claro, escolha claro.
   - Comentários explicando **o que** e **por quê** em cada bloco importante.
3. **Toda versão paralela usa a mesma ideia** (fácil de explicar):
   corta o vetor em N pedaços → cada thread ordena o seu pedaço com o algoritmo serial →
   junta (merge) os pedaços ordenados no final.
4. **Toda ordenação é conferida** (o programa verifica se o vetor saiu ordenado).
5. **Resultados sempre em CSV** (pasta `resultados/`), gráficos em PNG (pasta `graficos/`).
6. **Pelo menos 5 amostras** por combinação (algoritmo × versão × tipo de dado × tamanho × threads).
