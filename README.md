---

### *Branch com os arquivos*
Master

### *Nome do Aplicativo*
FloodGuard

### *Breve Descrição da Solução*
O FloodGuard é um aplicativo de protótipo para a plataforma Android, desenvolvido para o monitoramento e visualização de riscos de alagamento. A solução utiliza o Google Maps para exibir um mapa interativo com marcadores que representam tanto sensores IoT simulados quanto áreas de risco de inundação conhecidas. O usuário pode navegar pelo mapa, selecionar pontos de interesse para ver informações detalhadas em um painel, e alternar entre temas de mapa para melhor visualização. A navegação é facilitada por um menu lateral que lista todas as áreas de risco monitoradas.

### *Tecnologias Utilizadas (na versão atual)*

* *Linguagem Principal:* Kotlin
* *Plataforma:* Android SDK
* *Bibliotecas de Interface:*
    * Android Views com XML
    * Componentes do Material Design (Toolbar, NavigationDrawer, FloatingActionButton, BottomSheet, CardView)
    * AndroidX Core & AppCompat
    * Core Splashscreen (para a tela de abertura)
* *Serviços de Mapa e Localização:*
    * Google Maps SDK for Android
    * Google Play Services Location
* *Fonte de Dados Atual:* Dados estáticos (simulados) em listas dentro do código-fonte.
* *Ferramentas de Desenvolvimento:*
    * Android Studio
    * Gradle
    * Git / GitHub

### *Login e Senha do Usuário (para Futura Implementação)*

O projeto foi estruturado para futuramente incluir um sistema de autenticação. As telas de Login e Cadastro foram criadas, mas ainda não estão conectadas à tela principal do mapa.

Para fins de teste e desenvolvimento futuro, as credenciais a serem utilizadas são:

* *E-mail:* ana.silva@gmail.com
* *Senha:* senha123

*(Observação: Atualmente, o aplicativo abre diretamente na tela do mapa, sem exigir login.)*

### *Instruções Básicas de Execução*

Para compilar e rodar este projeto, são necessários dois passos principais:

*1. Configurar a Chave da API do Google Maps (Obrigatório)*
   * O projeto não funcionará sem uma chave de API válida do Google Maps.
   * *Obtenha a Chave:* Vá ao [Google Cloud Console](https://console.cloud.google.com/), crie um projeto, ative a API *"Maps SDK for Android"* e gere uma "Chave de API".
   * *Restrinja a Chave:* É crucial restringir a chave para o seu app. Adicione uma restrição "Apps Android" com:
      * *Nome do pacote:* com.enzop.floodguard
      * *Impressão digital SHA-1:* Execute o comando ./gradlew signingReport no terminal do Android Studio para obter esta chave.
   * *Adicione a Chave ao Projeto:* Abra o arquivo app/src/main/AndroidManifest.xml e cole sua chave no campo android:value da tag <meta-data> correspondente.

*2. Rodar o Aplicativo*
   * Abra o projeto no Android Studio.
   * Aguarde a sincronização do Gradle terminar com sucesso.
   * Conecte um dispositivo Android ou inicie um emulador.
   * Clique no botão de *Play (▶)* para instalar e executar o aplicativo.
