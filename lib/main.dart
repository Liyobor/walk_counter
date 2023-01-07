import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:walk_counter/logic.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  final logic = Get.put(Logic());
  logic.addEVentChannel();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatelessWidget {
  final logic = Get.put(Logic());
  MyHomePage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("test"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Steps:',
            ),
            Obx(() {
              return Text(
                '${logic.step.value}',
                style: Theme.of(context).textTheme.headline4,
              );
            }),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: logic.resetCount,
        tooltip: 'Increment',
        child: const Icon(Icons.lock_reset),
      ),
    );
  }

}