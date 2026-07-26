# Infinitory — 1.16.5 Port

Port não-oficial do mod **Infinitory** de Furgl (originalmente para Fabric 1.17.1) para **Minecraft 1.16.5**.

O mod original é open source sob licença MIT (https://github.com/Furgl/Infinitory), o que permite
legalmente copiar/adaptar o código — este port foi construído a partir do código-fonte real do
autor, mas com algumas partes reescritas do zero para funcionar de forma mais simples e segura
numa versão diferente do jogo (ver seção "O que foi simplificado" abaixo). A licença MIT original
está preservada em `LICENSE_Infinitory`.

## ⚠️ Importante: isto não foi compilado nem testado no jogo

Eu (Claude) não tenho acesso à internet neste ambiente, então não consigo baixar o Minecraft,
as mappings do Yarn nem a Fabric API para compilar e testar isto de verdade dentro do jogo.
O código foi escrito com bastante cuidado e é consistente internamente, mas é bem possível que
apareçam **1-3 pequenos erros de compilação** (nomes de método/campo que mudaram entre 1.16.5 e
1.17.1) na primeira vez que você rodar `gradle build`. Isso é normal em portes de mods entre
versões — o próprio autor original teria passado por isso. As seções "Pontos de maior risco"
abaixo dizem exatamente onde procurar se algo não compilar.

## Funcionalidades

- **Stack infinito**: qualquer item pode empilhar até `Config.maxStackSize` (padrão: 1 bilhão).
- **Inventário que se expande sozinho**: quando as linhas atuais enchem, uma nova linha de 9
  slots é liberada automaticamente, até `Config.maxExtraSlots` (padrão: 81 linhas extras).
- **Ordenação**: um botão "S" no canto do inventário alterna entre Nenhuma / Nome / Quantidade / ID
  e reordena os itens (a barra de acesso rápido não é reordenada).
- Tudo é salvo corretamente no save do mundo (incluindo itens nos slots extras).

## O que foi simplificado em relação ao mod original (1.17.1)

Para reduzir o risco de algo quebrar sem eu poder testar, algumas partes foram feitas de um jeito
mais simples do que o mod original:

- Os slots extras aparecem **abaixo** da barra de acesso rápido, em vez de intercalados antes dela.
- Não há tela de configurações (ModMenu/Cloth Config) — edite `config/infinitory.properties` e
  reinicie o jogo.
- Sem opção de crafting 3x3 expandido.
- Sem opção de "o que soltar ao morrer" — a morte solta tudo normalmente.
- Itens não-empilháveis (ferramentas, etc.) não podem ocupar o mesmo slot em múltiplas unidades.
- Equipar armadura com shift-click não é automático (a peça só vai para o inventário geral).
- Sem suporte à Trinkets API (mod de acessórios) que existia no mod original.

Nada disso afeta as três funcionalidades principais (stack infinito, expansão automática,
ordenação).

## Como compilar (sem computador, só com o celular)

Este projeto já vem com um workflow do GitHub Actions (`.github/workflows/build.yml`) que
compila o mod na nuvem — você só precisa colocar os arquivos num repositório do GitHub. Veja o
passo a passo completo na conversa com o Claude, ou resumidamente:

1. Instale o **Termux** (pela F-Droid, não pela Play Store).
2. No Termux: `pkg install git unzip` e `termux-setup-storage`.
3. Extraia este projeto e rode `git init`, `git add .`, `git commit`, e dê `git push` para um
   repositório novo criado em github.com (use um Personal Access Token como senha).
4. Abra a aba **Actions** do repositório no navegador do celular e espere o build terminar.
5. Baixe o artefato `infinitory-1.16.5-jar` gerado — dentro dele está o `.jar` do mod.

## Como compilar (com computador)

Requisitos: JDK 8+ (recomendado JDK 8 ou 11), acesso à internet na sua máquina (para o Gradle
baixar o Minecraft, as mappings e a Fabric API na primeira execução).

```bash
cd infinitory-1.16.5
./gradlew build      # Linux/Mac
gradlew.bat build    # Windows
```

Se o `gradlew`/`gradlew.bat` não existir (não incluí os binários do wrapper), rode isto uma vez
com um Gradle instalado localmente para gerá-los, ou simplesmente abra a pasta no IntelliJ IDEA
com o plugin do Fabric — ele importa o `build.gradle` automaticamente:

```bash
gradle wrapper --gradle-version 6.9
```

O `.jar` final aparece em `build/libs/infinitory-1.0.0.jar`. Instale normalmente na pasta `mods`
de uma instância Fabric 1.16.5 (com Fabric Loader + Fabric API instalados).

## Pontos de maior risco (onde checar primeiro se `gradle build` falhar)

1. **`PlayerInventoryMixin`** — os nomes `canStackAddMore`, `addStack`, `offer`,
   `removeStack`, `markDirty` foram confirmados a partir do código-fonte real de 1.17.1 e
   provavelmente são idênticos em 1.16.5, mas vale conferir com autocomplete do seu IDE.
2. **`stack.toTag(itemTag)` / `ItemStack.fromTag(itemTag)`** em `PlayerInventoryMixin` — a
   API de NBT do Minecraft foi renomeada entre essas versões (`CompoundTag`/`ListTag` no
   1.16.5 viraram `NbtCompound`/`NbtList` mais tarde). Usei os nomes que acredito serem os
   corretos para 1.16.5; se o compilador reclamar, seu IDE vai sugerir o nome certo.
3. **`InventoryScreenMixin`** — a parte de renderização do cliente (nomes exatos de
   `backgroundHeight`, `drawBackground`, `addButton`, `fill`) é a que tenho menos certeza
   absoluta. Se só essa classe der erro, o resto do mod (lógica do servidor) continua
   funcionando; você pode comentar o conteúdo dela temporariamente para testar o resto.

## Créditos

- Mod original **Infinitory**: [Furgl](https://github.com/Furgl) (MIT License, 2021)
- Port para 1.16.5: adaptado com apoio do Claude (Anthropic)
