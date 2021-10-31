package example.form.client.resolver

class UrlResolver:
  def heroRegister = "/hero/register"
  def heroCheckExistName(name: String) = s"/hero/exists?name=$name"
