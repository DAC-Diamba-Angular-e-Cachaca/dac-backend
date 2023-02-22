const dotenv = require('dotenv');
const jwt = require('jsonwebtoken');
const http = require('http');
const express = require('express');
const httpProxy = require('express-http-proxy');
const cookieParser = require('cookie-parser');
const bodyParser = require('body-parser');
const logger = require('morgan');
const helmet = require('helmet');
const cors = require('cors');

// Configs
const app = express();
const PORT = 5000;
dotenv.config({ path: `${process.env.NODE_ENV !== undefined ? '.env.dev' : '.env'}` });
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(logger('dev'));
app.use(helmet());
app.use(cookieParser());
app.use(cors());

// Proxy
const loginServiceProxy = httpProxy(process.env.HOST_AUTENTICACAO, {
  proxyReqBodyDecorator: function (bodyContent, srcReq) {
    try {
      const { login, senha } = bodyContent;
      const retBody = {};
      retBody.email = login;
      retBody.senha = senha;
      bodyContent = retBody;
    } catch (e) {
      console.log('ERRO: ' + e);
    }
    return bodyContent;
  },
  proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
    proxyReqOpts.headers['Content-Type'] = 'application/json';
    proxyReqOpts.method = 'POST';
    return proxyReqOpts;
  },
  userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
    if (proxyRes.statusCode === 200) {
      const str = Buffer.from(proxyResData).toString('utf-8');
      const objBody = JSON.parse(str);
      const id = objBody.id;
      const token = jwt.sign({ id }, process.env.SECRET, { expiresIn: 300 });
      userRes.status(200);
      return { auth: true, token, data: objBody };
    } else {
      userRes.status(401);
      return { message: 'Login inválido!' };
    }
  },
});

// Auxiliar functions
function verifyJWT(req, res, next) {
  const token = req.headers['x-access-token'];
  if (!token) return res.status(401).json({ auth: false, message: 'Token não fornecido.' });

  jwt.verify(token, process.env.SECRET, (err, decoded) => {
    if (err) return res.status(500).json({ auth: false, message: 'Falha ao autenticar o token.' });
    req.userId = decoded.id;
    next();
  });
}

// AUTENTICACAO
app.post(process.env.PATH_AUTENTICACAO + '/login', (req, res, next) => {
  loginServiceProxy(req, res, next);
});

app.post(process.env.PATH_AUTENTICACAO + '/logout', verifyJWT, (req, res) => {
  res.json({ auth: false, token: null });
});

app.get(process.env.PATH_AUTENTICACAO + '/list', verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_AUTENTICACAO, {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return { usuarios: objBody };
      } else {
        userRes.status(401);
        return { message: 'Um erro ocorreu ao buscar usuários.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_AUTENTICACAO}/:id`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_AUTENTICACAO, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o usuário.' };
      }
    },
  })(req, res, next);
});

// CRIAÇÃO CLIENTE + CONTA
app.post(process.env.PATH_ORQUESTRADOR + '/cliente', async (req, res, next) => {
  console.log(req.body)
  httpProxy(process.env.HOST_ORQUESTRADOR+'/cliente', {
    userResDecorator: function (proxyRes, _proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 201) {
        userRes.status(201);
        return { message: 'Cadastro realizado com sucesso. Consulte seu e-mail para próximos passos.' };
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Náo rodou' };
      }
    },
  })(req, res, next);
});

app.put(`${process.env.PATH_CLIENTE}/:id`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CLIENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao alterar o cliente.' };
      }
    },
  })(req, res, next);
});

app.get(process.env.PATH_CLIENTE + '/list', verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CLIENTE, {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return { clientes: objBody };
      } else {
        userRes.status(401);
        return { message: 'Um erro ocorreu ao buscar clientes.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CLIENTE}/por-cpf/:cpf`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_CLIENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o cliente.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CLIENTE}/por-email/:email`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_CLIENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o cliente.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CLIENTE}/:id`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_CLIENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o cliente.' };
      }
    },
  })(req, res, next);
});

// CONTA
app.post(process.env.PATH_CONTA + '/novo', async (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    userResDecorator: function (proxyRes, _proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 201) {
        userRes.status(201);
        return { message: 'Conta criada com sucesso.' };
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao criar a conta.' };
      }
    },
  })(req, res, next);
});

app.put(`${process.env.PATH_CONTA}/:id`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao alterar a conta.' };
      }
    },
  })(req, res, next);
});

app.get(process.env.PATH_CONTA + '/list', verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return { clientes: objBody };
      } else {
        userRes.status(401);
        return { message: 'Um erro ocorreu ao buscar contas.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CONTA}/por-usuario/:userId`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar a conta do usuário.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CONTA}/por-gerente/:gerenteId`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return { clientes: objBody };
      } else {
        userRes.status(401);
        return { message: 'Um erro ocorreu ao buscar contas por gerente.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CONTA}/pendentes/:gerenteId`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return { clientes: objBody };
      } else {
        userRes.status(401);
        return { message: 'Um erro ocorreu ao buscar contas pendentes por gerente.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_CONTA}/:id`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_CONTA, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar a conta.' };
      }
    },
  })(req, res, next);
});

//TRANSACAO
app.post('/transacaos', async (req, res, next) => {
  httpProxy('http://localhost:5003', {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
        return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      proxyReqOpts.method = 'POST';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, _proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        userRes.status(200);
        return { message: 'Transacao inserida com sucesso.' };
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao inserir transação!!!' };
      }
    },
  })(req, res, next);
});

app.get('/transacaos', async (req, res, next) => {
  httpProxy('http://localhost:5003', {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      proxyReqOpts.method = 'GET';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return objBody;
      } else {
       userRes.status(401);
       return { message: 'Um erro ocorreu ao buscar as transações.' };
      }
    },
  })(req, res, next);
});

// GERENTE
app.post(process.env.PATH_GERENTE + '/novo', verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    userResDecorator: function (proxyRes, _proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 201) {
        userRes.status(201);
        return { message: 'Gerente criado com sucesso.' };
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao criar gerente.' };
      }
    },
  })(req, res, next);
});

app.put(`${process.env.PATH_GERENTE}/:id`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao alterar o gerente.' };
      }
    },
  })(req, res, next);
});

app.delete(`${process.env.PATH_GERENTE}/:id`, verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    userResDecorator: function (proxyRes, _proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        userRes.status(200);
        return { message: 'Gerente excluído com sucesso.' };
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao alterar o gerente.' };
      }
    },
  })(req, res, next);
});

app.get(process.env.PATH_GERENTE + '/list', verifyJWT, async (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    proxyReqBodyDecorator: function (bodyContent, srcReq) {
      return bodyContent;
    },
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      if (proxyRes.statusCode === 200) {
        const str = Buffer.from(proxyResData).toString('utf-8');
        const objBody = JSON.parse(str);
        userRes.status(200);
        return { gerentes: objBody };
      } else {
        userRes.status(401);
        return { message: 'Um erro ocorreu ao buscar gerentes.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_GERENTE}/por-cpf/:cpf`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o gerente.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_GERENTE}/por-email/:email`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o gerente.' };
      }
    },
  })(req, res, next);
});

app.get(`${process.env.PATH_GERENTE}/:id`, verifyJWT, (req, res, next) => {
  httpProxy(process.env.HOST_GERENTE, {
    userResDecorator: function (proxyRes, proxyResData, _userReq, userRes) {
      if (proxyRes.statusCode == 200) {
        var str = Buffer.from(proxyResData).toString('utf-8');
        userRes.status(200);
        return str;
      } else {
        userRes.status(proxyRes.statusCode);
        return { message: 'Um erro ocorreu ao buscar o gerente.' };
      }
    },
  })(req, res, next);
});

// SERVER
const server = http.createServer(app);
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
