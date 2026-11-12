# Política de privacidade do Magic AI Ball (Wear OS e celulares e tablets Android)

**Data de vigência:** 12 de novembro de 2026

Esta política explica como o app Magic AI Ball (pacote `com.lorenzosuarez.magiceightball`), desenvolvido por Lorenzo Suarez, trata as informações. Ela abrange as duas versões do app: a de relógios Wear OS e a de celulares e tablets Android. Onde elas diferem, esta política indica isso.

## Resumo
- Não há conta nem login. Em celulares e tablets, e somente com o seu consentimento quando a lei o exige, o app contabiliza como é usado (nunca o que você pergunta nem o que ele responde), conforme descrito em "Medição de uso". O app do relógio não mede nada. Se o app falhar, um relatório técnico de falhas é enviado ao desenvolvedor, conforme descrito em "Relatórios de falhas". O app do relógio não exibe anúncios. O app de celulares e tablets pode exibir um anúncio ocasional, conforme descrito em "Anúncios"; antes disso, o seu consentimento é solicitado quando a lei o exige.
- As únicas coisas que o app salva no seu dispositivo são as suas escolhas no app (idioma, estilo de resposta, se você usa as respostas com IA e, em celulares e tablets, se usa as perguntas por voz e as respostas faladas, e os nomes que você digita para o modo festa) e, em celulares e tablets, uma lista das suas últimas perguntas e respostas ("Recentes"). O desenvolvedor não opera nenhum servidor que receba os seus dados.
- Uma resposta só sai do app para outro app ou outra pessoa quando você escolhe "Compartilhar" abaixo dela, conforme descrito em "Compartilhamento de uma resposta".
- Você pode desativar as respostas com IA no app a qualquer momento.
- O app se conecta a serviços do Google, todos descritos abaixo: o Firebase Remote Config, para consultar as configurações do app; a Gemini API, quando as respostas com IA estão ativadas; o Firebase Crashlytics, quando o app falha; e, em celulares e tablets, o Firebase Cloud Messaging para as notificações, o Google Analytics for Firebase quando o seu consentimento permite e o Google AdMob enquanto os anúncios estão ativados.
- **Perguntar em voz alta:** nas duas versões você pode segurar e dizer a sua pergunta. O app usa o microfone somente enquanto está ouvindo uma pergunta que você iniciou, a sua fala é transformada em texto pelo serviço de reconhecimento de fala do seu dispositivo, e esse texto é enviado à Gemini API somente quando as respostas com IA estão ativadas. O app nunca armazena a sua voz. Em celulares e tablets, o texto da sua pergunta é salvo, com a resposta, somente na lista "Recentes" do seu dispositivo, que você pode apagar a qualquer momento; o relógio não salva nada. O app funciona sem o microfone.
- **No relógio**, segurar para perguntar só é oferecido enquanto as respostas com IA estão em uso, porque somente o serviço de IA consegue responder a uma pergunta; caso contrário, o relógio responde quando é agitado e nunca escuta.

## O que o app envia, e para quem
**Firebase Remote Config.** Quando o app é iniciado, e no máximo cerca de uma vez por hora enquanto você o usa, ele solicita ao serviço Firebase Remote Config, do Google, as configurações atuais do app, por exemplo se as respostas com IA estão ativadas. Essa solicitação inclui um identificador de instalação aleatório criado pelo Firebase para esta cópia do app, além de informações básicas do app e do dispositivo (por exemplo, versão do app, idioma, país e versão do sistema operacional). Ela não inclui o seu nome, a sua conta nem a sua localização. O Google trata essas informações de acordo com os [termos de privacidade e segurança do Firebase](https://firebase.google.com/support/privacy).

**Gemini API.** As respostas com IA são usadas somente quando tanto você quanto o desenvolvedor as mantêm ativadas: você escolhe em "Respostas com IA", nas opções do app (ativadas por padrão), e o desenvolvedor pode ativá-las ou desativá-las para todas as pessoas. Quando o desenvolvedor as mantém desativadas, essa página as mostra como indisponíveis. Enquanto as respostas com IA estão desativadas, por escolha sua ou do desenvolvedor, o app mostra respostas de uma lista incorporada ao app e não envia nada à Gemini API. O mesmo vale enquanto o estilo de resposta "Clássico" está escolhido: as respostas dele são sempre as clássicas incorporadas ao app, independentemente do que mais estiver ativado. Enquanto as respostas com IA estão ativadas, perguntar à bola envia uma solicitação, por uma conexão HTTPS criptografada, à Gemini API do Google (`generativelanguage.googleapis.com`). Essa solicitação contém apenas:
- uma instrução escrita pelo desenvolvedor, que reflete o estilo de resposta e o idioma da resposta (inglês, espanhol ou português) que você escolheu no app, junto com o que o app sorteia para cada resposta para que não haja duas iguais: se ela deve ser um sim, um não ou uma resposta indefinida, e um tema e um modo de dizer tirados de listas incorporadas ao app. Nada nela vem de você;
- quando você agita, ou toca em um celular ou tablet: a palavra fixa "shake";
- quando você perguntou em voz alta e a sua pergunta foi reconhecida: o texto dessa pergunta (no máximo 200 caracteres) no lugar da palavra fixa, para que a resposta responda a ela;
- configurações técnicas de geração e a chave de API do desenvolvedor.

Nas duas versões, a solicitação nunca inclui áudio e não inclui o seu nome, os nomes digitados para o modo festa, a sua conta, os seus contatos, a sua localização, identificadores do dispositivo nem leituras de sensores. Por favor, não diga em uma pergunta nada que você não gostaria de enviar ao Google. Como em qualquer solicitação pela internet, o Google recebe o endereço IP do seu dispositivo e informações padrão de conexão. O Google trata essas solicitações de acordo com os [Termos de Serviço Adicionais da Gemini API](https://ai.google.dev/gemini-api/terms) e com a [Política de Privacidade do Google](https://policies.google.com/privacy). Dependendo de como a API é acessada, o Google pode manter essas solicitações por um período e usá-las para fornecer e melhorar os próprios serviços.

Se a solicitação falhar ou o seu dispositivo estiver offline, o app mostra, em vez disso, uma resposta da lista incorporada. O mesmo acontece depois que um dispositivo pediu a sua cota diária de respostas com IA; para manter essa contagem, o app salva no seu dispositivo a data de hoje e um número, e nada mais.

Cada solicitação também identifica o app que a está fazendo (o nome do pacote e a impressão digital do certificado de assinatura dele), para que o Google possa recusar solicitações que não venham deste app.

## Relatórios de falhas
Se o app parar de funcionar inesperadamente, ele envia um relatório de falhas ao serviço Firebase Crashlytics, do Google, na próxima vez em que for aberto, para que o desenvolvedor possa encontrar e corrigir o defeito. As duas versões do app fazem isso.

- **O que um relatório contém.** O rastreamento técnico da falha (em que ponto do código do app ela ocorreu), a versão do app, o modelo do dispositivo, a versão do sistema operacional, o idioma e a orientação do dispositivo, a memória e o armazenamento livres naquele momento, a data e a hora, e um identificador de instalação aleatório criado pelo Crashlytics para esta cópia do app.
- **O que ele nunca contém.** As suas perguntas, as respostas da bola, a sua lista "Recentes", áudio, o seu nome, a sua conta ou a sua localização. O app não acrescenta nada por conta própria a um relatório de falhas. (Uma resposta que você escolhe denunciar é enviada separadamente: veja "Denúncia de uma resposta".)
- **Quem o recebe.** O Google, em nome do desenvolvedor, de acordo com os [termos de privacidade e segurança do Firebase](https://firebase.google.com/support/privacy). Os relatórios são mantidos por 90 dias e depois excluídos. Eles são usados somente para corrigir o app, nunca para anúncios.
- **Quando o app não falhou.** Cada vez que o app é aberto, ele envia ao Crashlytics um breve registro de sessão (versão do app, modelo do dispositivo, versão do sistema operacional e o mesmo identificador de instalação aleatório), que é o que permite ao desenvolvedor ver quantas sessões terminam sem falha. Esse registro não contém nada sobre o que você faz no app.

## Medição de uso (apenas celulares e tablets)
Para saber quais partes do app são usadas e se uma alteração o melhorou, o app de celulares e tablets contabiliza algumas coisas que você faz nele, com o Google Analytics for Firebase. O app do relógio não faz isso.

- **O que é contabilizado.** Que a bola respondeu e, para essa resposta: se você tocou, agitou, falou ou usou "Quem de vocês…?", o estilo de resposta em uso, se a resposta foi escrita por IA ou veio da lista incorporada, e se ela tinha uma segunda linha. Que você compartilhou uma resposta ou o app. Que você escolheu um estilo de resposta ou ativou ou desativou um dos interruptores das opções, e qual. Que o modo festa foi configurado, e para quantas pessoas.
- **O que nunca é contabilizado.** As suas perguntas, as respostas da bola, os nomes digitados para o modo festa, a sua lista "Recentes", áudio ou qualquer outra coisa que você tenha dito ou escrito. O app não tem como incluí-los no que contabiliza.
- **O que vai junto.** O Google Analytics acrescenta o que coleta para todos os apps: um identificador aleatório criado para esta cópia do app, a versão do app, o modelo e o sistema operacional do dispositivo, o idioma e o país, e quando e por quanto tempo o app foi usado. O app desativou a coleta, pelo Google Analytics, do seu ID de publicidade e do ID do próprio dispositivo.
- **O seu consentimento.** Quando a lei o exige (por exemplo, no Espaço Econômico Europeu, no Reino Unido e na Suíça), nada é contabilizado a menos que você tenha concordado com isso na mensagem de consentimento: com o armazenamento de informações no seu dispositivo, com a medição e com a melhoria do serviço. Enquanto essa mensagem não for respondida, também nada é contabilizado. Você pode mudar a sua escolha a qualquer momento em "Privacidade de anúncios", nas opções, e a contabilização acompanha essa escolha.
- **Não é para publicidade.** O app instrui o Google Analytics a não usar essas contagens para publicidade nem para personalizar anúncios.
- **Quem recebe.** O Google, em nome do desenvolvedor, de acordo com os [termos de privacidade e segurança do Firebase](https://firebase.google.com/support/privacy). As contagens são mantidas por no máximo 14 meses.
- **Links compartilhados.** O link do Google Play que o app coloca no que você compartilha indica que ele foi compartilhado a partir do app, para que o desenvolvedor possa ver quantas instalações vêm de compartilhamentos. Ele não diz nada sobre você.

## Notificações (apenas celulares e tablets)
O desenvolvedor pode enviar, de vez em quando, uma notificação a quem usa o app, sobre alguma novidade no app ou como lembrete para voltar, por meio do Firebase Cloud Messaging, do Google. O app do relógio não recebe nenhuma.

- **O veredito do dia.** Uma vez por dia, o app pode mostrar uma frase na voz do estilo de resposta que você escolheu. Ela é criada no seu dispositivo a partir das respostas incorporadas ao app: nada é enviado nem buscado para isso, e ela não é mostrada em um dia em que você já perguntou à bola. Você pode desativá-la em "Veredito do dia", nas opções.
- **A sua permissão.** No Android 13 e versões posteriores, o app não mostra nenhuma notificação a menos que você permita. Ele pede uma única vez, e somente depois que você usou o app em vários dias. Você pode mudar a sua resposta a qualquer momento nas configurações do seu dispositivo, nas notificações do app.
- **Como uma mensagem chega até você.** As mensagens são enviadas a tópicos, não a pessoas: cada cópia do app segue um tópico comum a todas e um do idioma em que está configurada, para que uma mensagem possa ser escrita no seu idioma. Para entregá-las, o Firebase Cloud Messaging atribui a esta cópia do app um token de registro, vinculado a um identificador de instalação aleatório criado pelo Firebase. Nenhum dos dois diz quem você é, e o app não envia com eles nada das suas perguntas, respostas ou escolhas, exceto o idioma.
- **Quem recebe.** O Google, em nome do desenvolvedor, de acordo com os [termos de privacidade e segurança do Firebase](https://firebase.google.com/support/privacy).

## Anúncios (apenas celulares e tablets)
O app de celulares e tablets pode exibir, de tempos em tempos, um anúncio em tela cheia, somente depois que você fechou uma resposta e nunca enquanto você está perguntando ou lendo uma. O desenvolvedor pode ativar e desativar os anúncios para todas as pessoas; enquanto eles estão desativados, nada do que é descrito nesta seção acontece. O app do relógio nunca exibe anúncios.

- **Um anúncio que você escolhe assistir.** Depois que você usou as respostas com IA do dia, as opções podem oferecer mais delas em troca de assistir a um anúncio. Ele só é exibido se você pedir, e é solicitado e tratado como os outros anúncios descritos aqui.
- **Quem os exibe.** Os anúncios são fornecidos pelo Google AdMob. Para solicitar e exibir um anúncio, o software de publicidade do Google incluído no app coleta e usa informações como o ID de publicidade do seu dispositivo, o endereço IP, a localização aproximada derivada desse endereço, informações do dispositivo e do app, e como você interage com os anúncios. O Google trata essas informações de acordo com a [política do Google sobre como ele usa as informações de apps que usam os serviços dele](https://policies.google.com/technologies/partner-sites) e com a [Política de Privacidade do Google](https://policies.google.com/privacy).
- **O seu consentimento.** Quando a lei o exige (por exemplo, no Espaço Econômico Europeu, no Reino Unido e na Suíça), o app pede a sua escolha antes que qualquer anúncio seja solicitado, por meio da mensagem de consentimento do Google. Você pode mudar essa escolha a qualquer momento em "Privacidade de anúncios", nas configurações do app. Você também pode redefinir ou excluir o seu ID de publicidade, ou desativar a personalização de anúncios, nas configurações do seu dispositivo.
- **O que os anúncios nunca recebem.** As suas perguntas, as respostas da bola e a sua lista "Recentes" nunca são usadas para anúncios nem compartilhadas pelo app com o serviço de anúncios.
- **Permissão.** O software de publicidade adiciona a permissão `com.google.android.gms.permission.AD_ID` ao app de celulares e tablets.

## Perguntas por voz
Em um celular ou tablet, você segura a bola e diz a sua pergunta; no relógio, você segura a tela. Isso é opcional. Em um celular ou tablet, você pode desativar "Perguntas por voz" nas opções; em qualquer um dos dispositivos, você pode recusar a permissão do microfone, e então o app responde somente quando você toca ou agita. O relógio só oferece isso enquanto as respostas com IA estão em uso.

- **Quando o microfone é usado.** Somente enquanto você está segurando para perguntar (ou, em um celular ou tablet com um serviço de acessibilidade, entre a escolha de "Perguntar por voz" e o fim da sua pergunta), e somente enquanto o app está na tela. O app não escuta em nenhum outro momento e nunca em segundo plano.
- **Quem transforma a sua fala em texto.** O app não processa nem grava áudio por conta própria. Ele entrega a escuta ao serviço de reconhecimento de fala do seu dispositivo, por meio do recurso padrão de reconhecimento de fala do Android. Quando o seu dispositivo oferece reconhecimento no próprio dispositivo para o idioma do app (Android 12 e versões posteriores), o app o utiliza, e o áudio é processado no dispositivo. Caso contrário, o app usa o provedor padrão de reconhecimento de fala do seu dispositivo, que normalmente é o Google e que pode enviar o áudio aos próprios servidores para reconhecê-lo. Esse provedor trata o áudio de acordo com a própria política de privacidade (no caso do Google, a [Política de Privacidade do Google](https://policies.google.com/privacy)); você pode ver ou alterar o provedor nas configurações do seu dispositivo.
- **O que acontece com o texto.** A pergunta reconhecida é mostrada na tela, escrita como pergunta: um reconhecedor ouve palavras e não o tom em que foram ditas, por isso o próprio app acrescenta os pontos de interrogação. Se as respostas com IA estão ativadas, ela é enviada à Gemini API conforme descrito acima; se estão desativadas ou indisponíveis, o app não a envia a lugar nenhum (e o relógio não escuta de forma alguma).
- **O que é mantido.** O áudio nunca é salvo. Em celulares e tablets, a pergunta reconhecida é salva, junto com a resposta, na lista "Recentes" descrita abaixo, somente no seu dispositivo. O relógio mantém a pergunta na memória somente até que a resposta tenha sido mostrada.

## Respostas faladas (apenas celulares e tablets)
O app de celulares e tablets pode ler cada resposta em voz alta. Isso fica desativado até você ativar "Respostas faladas" nas opções, e você também pode pedir a leitura de uma única resposta com o botão de alto-falante mostrado enquanto ela está na tela.

- **Quem fala.** O app não produz a voz por conta própria. Ele entrega o texto da resposta, e nada mais, ao serviço de conversão de texto em voz do seu dispositivo, por meio do recurso padrão do Android. Esse serviço normalmente fala no próprio dispositivo; dependendo da voz instalada, ele pode enviar o texto aos servidores do provedor para produzir o áudio. O provedor (normalmente o Google) trata esse texto de acordo com a própria política de privacidade, e você pode ver ou alterar o provedor nas configurações do seu dispositivo.
- **O que nunca é entregue.** A sua pergunta, a sua lista "Recentes" e os nomes digitados para o modo festa. Uma resposta que seja um desses nomes, conforme descrito em "Modo festa", é lida em voz alta como qualquer outra resposta.
- **O que é mantido.** Nada: o app não salva nenhum áudio.

## Modo festa (apenas celulares e tablets)
Nas opções, você pode digitar os nomes das pessoas que estão jogando, de duas a oito. O app então mostra de quem é a vez e oferece "Quem de vocês…?", que responde com um desses nomes, escolhido aleatoriamente no dispositivo.

- **Onde ficam os nomes.** No arquivo de configurações do app no seu dispositivo, e em "Recentes" quando um nome foi uma resposta. Eles não são enviados ao desenvolvedor, à Gemini API nem a mais ninguém, e "Quem de vocês…?" não entra em contato com nenhum serviço.
- **O seu controle.** "Desativar", na caixa de diálogo do modo festa, apaga os nomes. Limpar os dados do app ou desinstalar o app também os apaga.
- Por favor, digite apenas primeiros nomes ou apelidos, e somente de pessoas que estão jogando com você.

## Compartilhamento de uma resposta (apenas celulares e tablets)
Abaixo de uma resposta, "Compartilhar" cria uma imagem dela e a entrega ao app que você escolhe no menu de compartilhamento do seu dispositivo. Nada é compartilhado a menos que você escolha isso, a cada vez.

- **O que há na imagem.** O nome do app, a sua pergunta, se você fez uma em voz alta, a resposta com a linha que o personagem acrescentou abaixo dela, e o nome do estilo de resposta que a deu.
- **O que vai junto.** Um texto curto com o link do app no Google Play.
- **Para onde vai.** Somente para o app que você escolhe, que a trata de acordo com os próprios termos. O desenvolvedor não recebe nada, e o app não fica sabendo qual app você escolheu.
- **O que é mantido.** A imagem é salva como um único arquivo no armazenamento temporário do app, para que o outro app possa lê-la, e é substituída pela próxima que você compartilhar. Ela é removida quando você limpa o cache ou os dados do app ou desinstala o app, e o seu dispositivo pode removê-la por conta própria.

## Denúncia de uma resposta (apenas celulares e tablets)

As respostas escritas por IA não são lidas por ninguém antes de você vê-las. Abaixo de uma resposta assim há um botão "Denunciar", para quando uma delas não deveria ter sido dada.

- **O que é enviado.** A resposta como foi escrita, com a linha que o personagem acrescentou abaixo dela, o estilo de resposta e o idioma. A sua pergunta não é enviada, e o app não acrescenta nada que identifique você.
- **Quem recebe.** O desenvolvedor, por meio do Firebase Crashlytics, o mesmo serviço do Google que recebe os relatórios de falhas, e sob os mesmos termos e a mesma retenção descritos em "Relatórios de falhas". A denúncia é usada somente para revisar o que a IA escreveu e para corrigir as instruções que ela recebe.
- **A sua escolha.** Nada é enviado a menos que você pressione "Denunciar". Uma denúncia não pode ser desfeita depois de enviada.

## Widget da tela inicial e bloco do relógio
O app de celulares e tablets oferece um widget para a tela inicial, e o app do relógio um bloco, que mostram uma resposta ao serem tocados. As respostas deles sempre vêm da lista incorporada ao app: eles não enviam nada à Gemini API, não usam o microfone, e as respostas deles não são salvas em "Recentes".

## Recentes: suas últimas perguntas e respostas (apenas celulares e tablets)
O app de celulares e tablets mantém uma lista das últimas respostas que a bola deu a você, para que você possa lê-las novamente. O app do relógio não mantém nenhuma lista desse tipo.

- **O que é salvo.** Para cada resposta: o texto da sua pergunta, se você perguntou em voz alta (nada se você tocou ou agitou; para "Quem de vocês…?" no modo festa, essas palavras e o nome que foi escolhido), a resposta, o estilo de resposta que a deu, a data e a hora, e se a resposta foi escrita por IA ou veio da lista incorporada ao app.
- **Onde.** Em um arquivo privado no armazenamento do próprio app, no seu dispositivo. Ele não é enviado ao desenvolvedor, ao Google nem a mais ninguém, não é sincronizado entre dispositivos e não é incluído em backups do dispositivo nem em transferências de um dispositivo para outro.
- **Quanto.** No máximo as 50 respostas mais recentes; as mais antigas são apagadas automaticamente à medida que chegam novas.
- **O seu controle.** Você pode apagar qualquer entrada individual (com o botão de apagar dela; "Desfazer" permite trazê-la de volta durante alguns segundos), ou todas elas com "Apagar tudo", na própria lista. Limpar os dados do app ou desinstalar o app também apaga a lista.
- **Compartilhamento.** A lista em si nunca é enviada a lugar nenhum. Uma resposta que está na tela pode ser compartilhada por você, conforme descrito em "Compartilhamento de uma resposta". "Compartilhar o app", nas opções, entrega ao app que você escolhe apenas um texto curto com o link do app no Google Play.

## Sensores e permissões
- **Sensor de movimento (acelerômetro):** usado somente no seu dispositivo para detectar quando ele é agitado e, em celulares e tablets, para mover levemente a bola conforme você move o dispositivo. As leituras do sensor não são armazenadas nem enviadas a lugar nenhum. Nenhuma permissão é necessária.
- **Internet e estado da rede** (`INTERNET`, `ACCESS_NETWORK_STATE`): usados para entrar em contato com o Firebase Remote Config, a Gemini API, o Firebase Crashlytics após uma falha e, em celulares e tablets quando o seu consentimento permite, o Google Analytics for Firebase.
- **Outros apps no seu dispositivo:** o app pergunta ao Android quais serviços de reconhecimento de fala e de conversão de texto em voz estão instalados, somente para poder ouvir uma pergunta e ler respostas em voz alta. Ele não lê mais nada sobre os seus apps.
- **Notificações** (`POST_NOTIFICATIONS`): solicitada uma única vez, conforme descrito em "Notificações". O app funciona da mesma forma sem ela.
- **Microfone** (`RECORD_AUDIO`): solicitada na primeira vez em que você segura para perguntar em voz alta, e usada somente conforme descrito em "Perguntas por voz". Você pode recusá-la ou revogá-la nas configurações do seu dispositivo a qualquer momento; o app continua funcionando quando você toca ou agita.

Nenhuma das duas versões solicita localização, câmera, contatos, sensores corporais nem qualquer outra permissão sensível.

## Informações armazenadas no seu dispositivo
O app salva as suas escolhas (idioma, estilo de resposta, se você usa as respostas com IA e, em celulares e tablets, se usa as perguntas por voz e as respostas faladas, e os nomes digitados para o modo festa) em um pequeno arquivo de configurações no seu dispositivo, para que elas continuem lá na próxima vez em que você abrir o app. Elas permanecem no dispositivo, não são enviadas a lugar nenhum por si sós e são removidas quando você limpa os dados do app ou o desinstala. Em celulares e tablets, o app também salva a lista "Recentes" descrita acima (no máximo as suas 50 perguntas e respostas mais recentes), que é removida da mesma forma ou a partir da própria lista. Em celulares e tablets, o app também guarda por quantos dias seguidos você perguntou à bola, na forma de dois números e uma data, que nunca saem do dispositivo. Em celulares e tablets, a imagem da última resposta que você compartilhou aguarda no armazenamento temporário, conforme descrito em "Compartilhamento de uma resposta". No relógio, a última resposta é mantida somente na memória. O app não armazena mais nada por conta própria. O Firebase armazena no dispositivo os próprios identificadores de instalação e as configurações mais recentes, e um relatório de falhas aguarda ali até ser enviado. Nada é incluído em backups do dispositivo.

## Retenção e exclusão
Com exceção dos relatórios de falhas descritos acima, que o Google mantém para o desenvolvedor por 90 dias, e das contagens de uso descritas em "Medição de uso", que o Google mantém por no máximo 14 meses, o desenvolvedor não coleta nem armazena as suas informações. O que o app salva no seu dispositivo permanece lá até que você o exclua, conforme descrito acima. As informações recebidas pelo Google, pelo provedor de reconhecimento de fala ou de conversão de texto em voz do seu dispositivo, ou por um app com o qual você compartilha uma resposta, são tratadas de acordo com as políticas deles, conforme descrito acima.

## Crianças
O app não é direcionado a crianças, e o desenvolvedor não coleta conscientemente dados pessoais de crianças.

## Alterações nesta política
Se esta política mudar, a versão atualizada será publicada aqui com uma nova data de vigência.

## Idiomas
Esta política é publicada em inglês, espanhol e português. Se as versões diferirem, prevalece a versão em inglês.

## Contato
Em caso de dúvidas sobre esta política, entre em contato com o desenvolvedor pelos dados de contato que constam na página do app no Google Play.
