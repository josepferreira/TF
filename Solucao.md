# Protótipo 1

## Sem considerar paralelismo nas execuções

- Podemos usar replicação ativa, apenas a esperar pela resposta de um servidor

- Garantimos a parte de que se um servidor falhar a tarefa é re-atribuido pelo protocolo de replicação ativa, visto que todos executam a tarefa

- O stub pode ser o middleware que conecta o cliente aos servidores

- Temos de pensar se faz sentido um caso em que no meio de um pedido falham os três servidores, em tempos diferentes, visto que pode não responder ao cliente (se bem que não penso que isto aconteça, é só para salvaguardar)

- Temos de pensar na transferência de estado:
    - se passamos apenas o que o servidor não conhece ou se passamos o estado todo
    - se usamos o total order multicast para ordenar os pedidos de transferencia de estado (penso q é o mais usual)

- Em termos de estado temos de ter:
    - uma fila FIFO com as ações
    - um conjunto de empresas e as suas ações disponíveis

### Transferência de estado
- Podemos ter um identificador incremental associoado a cada operação que chega, garantindo assim sempre ordem

- Quando queremos transferir estado indicamos qual o último identificador que conhecemos e o servidor que nos responder apenas responde com as operações com os identificadores superiores (os q ainda n conhecemos)

- Temos de guardar sempre em ficheiro!